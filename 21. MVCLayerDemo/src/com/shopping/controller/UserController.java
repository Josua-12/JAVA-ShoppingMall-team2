package com.shopping.controller;

import java.util.List;
import java.util.Scanner;

import com.shopping.model.User;
import com.shopping.Auth.Session;
import com.shopping.model.Admin;
import com.shopping.model.Order;
import com.shopping.model.Role;
import com.shopping.service.UserService;
import com.shopping.service.AuthService;
import com.shopping.service.OrderService;
import com.shopping.repository.DefaultFileOrderRepository;
import com.shopping.repository.FileAdminRepository;
import com.shopping.repository.FileOrderRepository;
import com.shopping.repository.FileUserRepository;


/**
 * 사용자 관련 UI를 담당하는 컨트롤러 
 * Presentation Layer의 일부로 사용자 입력을 받고 결과를 표시
 */
public class UserController {
	
	private UserService userService;
	private AuthService authService;
	private Scanner scanner;
	private OrderService orderService;
	private Session session;

	
	public UserController(Session session) {
	    FileUserRepository userRepo = new FileUserRepository();
	    FileAdminRepository adminRepo = new FileAdminRepository();
	    DefaultFileOrderRepository orderRepo = new DefaultFileOrderRepository("data/orders.dat");
	    this.userService = new UserService(userRepo);
	    this.authService = new AuthService(userRepo, adminRepo);
	    this.orderService = new OrderService(orderRepo, null, orderRepo);
	    this.scanner = new Scanner(System.in);
	    this.session = session;
	}
	
	public AuthService getAuthService() {
	    return authService;
	}

	
	// 마이페이지
	public void myPage(User user) {
		while(true) {
			System.out.println("\n╔════════════════════════════════════════════╗");
	        System.out.println("║                   마이 페이지                   ║");
	        System.out.println("╚══════════════════════════════════════════════╝\n");
	        
	        System.out.println("║1. 내 정보 조회                                  ║");
	        System.out.println("║2. 비밀번호 변경                                  ║");
	        System.out.println("║3. 개인정보 수정                                  ║");
	        System.out.println("║4. 주문 내역 조회                                 ║");
	        System.out.println("║5. 회원 탈퇴                                     ║");
	        System.out.println("║6. 돌아가기                                      ║");
	        System.out.println("╚══════════════════════════════════════════════╝\n");

	        String choice = scanner.nextLine();
	        
	        switch (choice) {
            case "1":
            	showUserInfo();
                break;
            case "2":
            	changePassword();
                break;
            case "3":
            	editPersonalInfo();
                break;
            case "4":
            	viewOrderHistory();
                break;
            case "5":
            	deleteAccount();
                break;
            case "6":
                return;   // 메뉴 종료
            default:
                System.out.println("잘못된 선택입니다.");
	        }
		}
	}
	// 회원 탈퇴
	private void deleteAccount() {
	    User user = userService.findById(session.getUserId());
	    if (user == null || session.getRole() != Role.USER) {
	        System.out.println("회원 전용 기능입니다.");
	        return;
	    }
	    System.out.print("정말 탈퇴하시겠습니까? (Y/N): ");
	    String confirm = scanner.nextLine();
	    if (confirm.equalsIgnoreCase("Y")) {
	        try {
	            userService.deleteUser(user.getId());
	            authService.logout();
	            session.logout();  // ★★★ 꼭 추가하세요!!
	            System.out.println("회원 탈퇴 완료. 이용해 주셔서 감사합니다.");
	        } catch (Exception e) {
	            System.out.println("회원 탈퇴 실패: " + e.getMessage());
	        }
	    } else {
	        System.out.println("탈퇴 취소");
	    }
	}
   
	        
    // 주문 내역 조회
    private void viewOrderHistory() {
        User user = (User) authService.getLoggedInUser();
        if (user == null) {
            System.out.println("로그인이 필요합니다.");
            return;
        }
        System.out.println("=== 주문 내역 ===");
        List<Order> orders = orderService.listOrders(user.getId(), Role.USER);

        if (orders.isEmpty()) {
            System.out.println("주문 내역이 없습니다.");
            return;
        }

        for (Order order : orders) {
            System.out.println("주문 ID: " + order.getOrderId() +
                               ", 상태: " + order.getStatus() +
                               ", 총 금액: " + order.getTotalPrice() +
                               ", 날짜: " + order.getOrderDate());
        }
    }     
	        
	// 개인정보 수정
    private void editPersonalInfo() {
        User user = (User) authService.getLoggedInUser();
        if (user == null) {
            System.out.println("로그인이 필요합니다.");
            return;
        }
        System.out.print("새 이름 입력: ");
        String newName = scanner.nextLine();
        System.out.print("새 이메일 입력: ");
        String newEmail = scanner.nextLine();
        try {
            userService.updatePersonalInfo(user.getId(), newName, newEmail);
            System.out.println("개인정보가 수정되었습니다.");
        } catch (Exception e) {
            System.out.println("개인정보 수정 실패: " + e.getMessage());
        }
    }        
	        
	// 비밀번호 변경
	private void changePassword() {
		User user = (User) authService.getLoggedInUser();
	    if (user == null) {
	        System.out.println("로그인이 필요합니다.");
	        return;
	    }
	    System.out.print("새 비밀번호 입력: ");
	    String newPwd = scanner.nextLine();
	    try {
	        userService.updatePassword(user.getId(), newPwd);
	        System.out.println("비밀번호가 변경되었습니다.");
	    } catch (Exception e) {
	        System.out.println("비밀번호 변경 실패: " + e.getMessage());
	    }
	}

	// 회원가입 처리
	public void register() {
		System.out.println("\n== 회원가입 ==");
		
		// 아이디 입력 받기
		System.out.print("아이디 (3자 이상, 영문/숫자): ");
		String id = scanner.nextLine();
		
		// 입력 검증
		if (id.length() < 3) {
			System.out.println("아이디는 3자 이상이어야 합니다.");
			return;
		}
		
		// 패스워드 입력 받기
		System.out.print("패스워드 (4자 이상): ");
		String password = scanner.nextLine();
		
		if (password.length() < 4) {
			System.out.println("패스워드는 4자 이상이어야 합니다.");
			return;
		}

		// 이메일 입력 받기
		System.out.print("이메일 (아이디@도메인): ");
		String email = scanner.nextLine();
		
		if (!email.contains("@") || !email.contains(".")) {
			System.out.println("올바른 이메일 형식이 아닙니다.");
			return;
		}
		
		// 이름 입력 받기
		System.out.print("이름: ");
		String name = scanner.nextLine();
		
		if (name.trim().isEmpty()) {
			System.out.println("이름을 입력해주세요.");
			return;
		}
		
		try {
			User user = userService.register(id, password, email, name);
			
			System.out.println("회원가입 성공!");
			System.out.println("환영합니다, " + user.getName() + "님!");
			System.out.println("초기 잔액: " + (int)user.getBalance() + "원");			
		} catch (Exception e) {
			System.out.println("회원가입 실패: " + e.getMessage());
		}
	}
	
	// 로그인 처리
	public void login() {
	    System.out.println("\n== 로그인 ==");
	    
	    if (session.isLoggedIn()) {
	        System.out.println("이미 로그인되어 있습니다.");
	        return;
	    }

	    System.out.print("이메일: ");
	    String email = scanner.nextLine();
	    System.out.print("패스워드: ");
	    String password = scanner.nextLine();
	    
	    try {
	        Role role = authService.login(email, password);  // Role 반환받기
	        Object loggedUser = authService.getLoggedInUser();
	        
	        if (role == Role.USER && loggedUser instanceof User user) {
	            session.login(user.getId(), Role.USER, user);
	            System.out.println("로그인 성공! 회원: " + user.getName());
	        } else if (role == Role.ADMIN && loggedUser instanceof Admin admin) {
	            session.login(admin.getId(), Role.ADMIN, null);
	            System.out.println("로그인 성공! 관리자: " + admin.getName());
	        }
	    } catch (Exception e) {
	        System.out.println("로그인 실패: " + e.getMessage());
	    }
	}




	public void logout() {
	    if (!session.isLoggedIn()) {
	        System.out.println("로그인된 사용자가 없습니다.");
	        return;
	    }
	    String name = (session.getUser() != null) ? session.getUser().getName() : session.getUserId();
	    authService.logout();
	    session.logout();
	    System.out.println(name + "님이 로그아웃 되었습니다.");
	}


	
	// 내 정보 보기
	private void showUserInfo() {
	    User user = session.getUser();  // 세션에서 User 객체 직접 읽기
	    if (user == null) {
	        System.out.println("로그인이 필요합니다.");
	        return;
	    }
	    System.out.println("\n== 내 정보 (회원) ==");
	    System.out.println("ID: " + user.getId());
	    System.out.println("이름: " + user.getName());
	    System.out.println("이메일: " + user.getEmail());
	    System.out.println("잔액: " + (int) user.getBalance() + "원");
	}

}
