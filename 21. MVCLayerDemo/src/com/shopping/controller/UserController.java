package com.shopping.controller;

import java.util.Scanner;

import com.shopping.model.User;
import com.shopping.model.Admin;
import com.shopping.model.Role;
import com.shopping.service.UserService;
import com.shopping.service.AuthService;
import com.shopping.repository.FileAdminRepository;
import com.shopping.repository.FileUserRepository;


/**
 * 사용자 관련 UI를 담당하는 컨트롤러 
 * Presentation Layer의 일부로 사용자 입력을 받고 결과를 표시
 */
public class UserController {
	
	private UserService userService;
	private AuthService authService;
	private Scanner scanner;
	
	public UserController() {
		FileUserRepository userRepo = new FileUserRepository();
		FileAdminRepository adminRepo = new FileAdminRepository();
		this.userService = new UserService(userRepo);
		this.authService = new AuthService(userRepo, adminRepo);
		this.scanner = new Scanner(System.in);
	}
	
	// 사용자 관리 메뉴 표시 및 처리 
	public void showUserMenu() {
		while(true) {
			System.out.println("\n== 사용자 메뉴 ==");
			System.out.println("1. 회원가입");
			System.out.println("2. 로그인");
			System.out.println("3. 로그아웃");
			System.out.println("4. 내 정보 보기");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				register();		// 회원가입
				break;
			case "2":
				login();		// 로그인
				break;
			case "3":
				logout();		// 로그아웃
				break;
			case "4":
				showUserInfo();	// 내 정보 보기
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
	}

	// 회원가입 처리
	private void register() {
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
	private void login() {
		System.out.println("\n== 로그인 ==");
		
		// 현재 로그인 상태 확인
		Object currentLoginObj = authService.getLoggedInUser();
		if (currentLoginObj != null) {
		    String userName = (currentLoginObj instanceof User ? ((User)currentLoginObj).getName() : ((Admin)currentLoginObj).getName());
		    System.out.println("이미 로그인되어 있습니다: " + userName);
		    return;
		}
		
		System.out.print("이메일: ");
		String email = scanner.nextLine();
		
		System.out.print("패스워드: ");
		String password = scanner.nextLine();
		
		try {
			Object loginObj = authService.login(email, password);
			
			if (loginObj instanceof User) {
	            User currentUser = (User) currentLoginObj;
	            Role userRole = currentUser.getRole();
	            System.out.println("로그인 성공! 회원: " + currentUser.getName() + "님");
	            System.out.println("역할: " + (userRole == Role.ADMIN ? "관리자" : "회원"));
	        } else if (currentLoginObj instanceof Admin) {
	            Admin admin = (Admin) currentLoginObj;
	            System.out.println("로그인 성공! 관리자: " + admin.getName() + "님");
	            System.out.println("관리 권한: " + (admin.canManageProducts() ? "있음" : "없음"));
	        }
		} catch (Exception e) {
			System.out.println("로그인 실패: " + e.getMessage());
		}
	}
	
	// 로그아웃 처리
	private void logout() {
		Object nowLoginObj = authService.getLoggedInUser();
		if (nowLoginObj == null) {
			System.out.println("로그인된 사용자가 없습니다.");
			return;
		}
		
		String userName = (nowLoginObj instanceof User ? ((User)nowLoginObj).getName() : ((Admin)nowLoginObj).getName());
		authService.logout();
		System.out.println(userName + "님이 로그아웃되었습니다.");
	}
	
	// 내 정보 보기
	private void showUserInfo() {
	    Object obj = authService.getLoggedInUser();
	    if (obj == null) {
	        System.out.println("로그인이 필요합니다.");
	        return;
	    }

	    if (obj instanceof User user) {
	        System.out.println("\n== 내 정보 (회원) ==");
	        System.out.println("ID: " + user.getId());
	        System.out.println("이름: " + user.getName());
	        System.out.println("이메일: " + user.getEmail());
	        System.out.println("잔액: " + (int)user.getBalance() + "원");
	    } else if (obj instanceof Admin admin) {
	        System.out.println("\n== 내 정보 (관리자) ==");
	        System.out.println("ID: " + admin.getId());
	        System.out.println("이름: " + admin.getName());
	        System.out.println("이메일: " + admin.getEmail());
	        System.out.println("역할: " + admin.getRole());
	        System.out.println("관리 권한: " + (admin.canManageProducts() ? "있음" : "없음"));
	    }
	}
}
