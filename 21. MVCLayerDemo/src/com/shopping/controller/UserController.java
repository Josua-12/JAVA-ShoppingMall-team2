package com.shopping.controller;

import java.util.Scanner;

import com.shopping.model.User;
import com.shopping.service.UserService;

/*
 * 사용자 관련 UI를 담당하는 컨트롤러 
 * Presentation Layer의 일부로 사용자 입력을 받고 결과를 표시
 */
public class UserController {
	
	private UserService userService;
	private Scanner scanner;
	
	public UserController() {
		this.userService = new UserService();
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
			
			//사용자선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				register();		//회원가입
				break;
			case "0":
				return;			//메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
	}

	//회원가입처리
	private void register() {
		System.out.println("\n== 회원가입 ==");
		
		//아이디 입력 받기
		System.out.print("아이디 (3자 이상, 영문/숫자):");
		String id = scanner.nextLine();
		
		//입력 검증
		
		//패스워드 입력 받기
		System.out.print("패스워드 (4자 이상):");
		String password = scanner.nextLine();
		
		//이름 입력 받기
		System.out.print("이름: ");
		String name = scanner.nextLine();
		
		try {
			User user = userService.register(id, password, name);
			
			System.out.println("회원가입 성공!");
			System.out.println("환영합니다. "+user.getName()+"님!");
			System.out.println("초기 잔액: "+user.getBalance()+" 원");			
		} catch (Exception e) {
			System.out.println("회원가입 실패 : " + e.getMessage());
		}

		
	}
}











