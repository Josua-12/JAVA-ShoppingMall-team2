package com.shopping.controller;

import java.util.Scanner;

import com.shopping.model.Admin;
import com.shopping.model.User;
import com.shopping.repository.FileUserRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.OrderService;
import com.shopping.service.UserService;

public class AdminController {
	private UserService userService;
	private OrderService orderService;
//	private ProductService productService;
	private Scanner scanner;
    private UserRepository userRepository;
    
	
	public AdminController() {
		//super();
		this.userRepository = new FileUserRepository();
		this.userService = new UserService();
		this.orderService = new OrderService(null, null);
//		this.productService = new ProductService;
		this.scanner = new Scanner(System.in);
	}
	
	
	
	// 관리자 기능 메뉴 표시 및 처리
	public void showAdminMenu() {
		while(true) {
			System.out.println("\n== 관리자 기능 ==");
			System.out.println("1. 상품 관리");
			System.out.println("2. 사용자 관리");
			System.out.println("3. 주문 관리");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				handleProductManagement();		// 상품 관리
				break;
			case "2":
				handleUserManagement();		// 사용자 관리
				break;
			case "3":
				handleOrderManagement();		// 주문 관리
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
	}



	private void handleOrderManagement() {
		while(true) {
			System.out.println("\n== 주문 관리 메뉴 ==");
			System.out.println("1. 전체 주문 목록 조회");
			System.out.println("2. 주문 상태 변경");
			System.out.println("3. 주문 통계 조회 (일별, 상품별)");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				//displayAllOrders();		// 전체 주문 목록 조회
				break;
			case "2":
				//updateOrderStatus();		// 주문 상태 변경
				break;
			case "3":
				//displayOrderStatistics();		// 주문 통계 조회
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
		
	}



	private void handleUserManagement() {
		while(true) {
			System.out.println("\n== 사용자 관리 메뉴 ==");
			System.out.println("1. 전체 회원 목록 조회");
			System.out.println("2. 회원 검색 (ID, 이름, 이메일)");
			System.out.println("3. 회원 상세 정보 조회");
			System.out.println("4. 회원 강제 탈퇴");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				displayAllUsers();		// 전체 회원 목록 조회
				break;
			case "2":
				searchUsers();		// 회원 검색 (ID, 이름, 이메일)
				break;
			case "3":
				displayUserDetails();		// 회원 상세 정보 조회
				break;
			case "4":
				//deactivateUser();		// 회원 강제 탈퇴
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
		
	}


	
	private void displayUserDetails() {
	    System.out.print("상세 정보를 볼 회원의 ID를 입력하세요: ");
	    String id = scanner.nextLine().trim();

	    User user = userRepository.findById(id);
	    if (user != null) {
	        System.out.println("\n=== 회원 상세 정보 ===");
	        System.out.println("ID: " + user.getId());
	        System.out.println("이름: " + user.getName());
	        System.out.println("이메일: " + user.getEmail());
	        System.out.println("권한(Role): " + user.getRole());
	        System.out.println("잔액: " + user.getBalance());
	        System.out.println("====================\n");
	    } else {
	        System.out.println("해당 ID의 회원이 존재하지 않습니다.");
	    }
	}




	private void searchUsers() {
		while(true) {
			System.out.println("\n== 회원 검색 ==");
			System.out.println("1. ID로 회원 검색");
			System.out.println("2. 이름으로 회원 검색");
			System.out.println("3. 이메일로 회원 검색");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				searchUsersById();		// ID로 회원 검색
				break;
			case "2":
				//searchUsersByName();		// 이름으로 회원 검색
				break;
			case "3":
				//searchUsersByEmail();		// 이메일로 회원 검색
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
		
	}



	private void searchUsersById() {
		System.out.println("검색할 유저의 ID를 입력하세요: ");
		String Id = scanner.nextLine().trim();
		
		userRepository.findById(Id).toString();
		
	}



	private void displayAllUsers() {
		System.out.println("\n== 전체 사용자 조회 ==");
		userRepository.findAll();
		
	}



	private void handleProductManagement() {
		while(true) {
			System.out.println("\n== 상품 관리 메뉴 ==");
			System.out.println("1. 신규 상품 등록");
			System.out.println("2. 상품 정보 수정");
			System.out.println("3. 상품 삭제");
			System.out.println("4. 재고 관리 (입고 처리)");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				//();		// 신규 상품 등록
				break;
			case "2":
				//();		// 상품 정보 수정
				break;
			case "3":
				//();		// 재고 관리 (입고 처리)
				break;
			case "4":
				//();		// 회원 강제 탈퇴
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
		
	}
}
