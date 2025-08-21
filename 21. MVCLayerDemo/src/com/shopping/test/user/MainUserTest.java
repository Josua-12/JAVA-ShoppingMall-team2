package com.shopping.test.user;

import com.shopping.Auth.Session;
import com.shopping.controller.UserController;
import com.shopping.model.Admin;
import com.shopping.model.User;

public class MainUserTest {
    public static void main(String[] args) {
        Session session = new Session();
        UserController userController = new UserController(session);

        System.out.println("=== 유저 기능 테스트 ===");
        

        while (true) {
            System.out.println("\n메뉴: 1.회원가입 2.로그인 3.마이페이지 4.로그아웃 5.종료");
            System.out.print("선택: ");
            String choice = new java.util.Scanner(System.in).nextLine();

            switch (choice) {
                case "1":
                    userController.register();  // 회원가입
                    break;
                case "2":
                    userController.login();     // 로그인
                    break;
                case "3":
                	if (session.isLoggedIn()) {
                	    Object loggedObj = userController.getAuthService().getLoggedInUser();
                	    if (loggedObj instanceof User user) {
                	        userController.myPage(user);
                	        System.out.println("세션유저: "+session.getUser());
                	    } else if (loggedObj instanceof Admin) {
                	        System.out.println("관리자 계정은 별도 관리자 페이지를 이용해주세요.");
                	    } else {
                	        System.out.println("로그인 정보가 없습니다.");
                	    }
                	} else {
                	    System.out.println("로그인 후 이용하세요.");
                	}
                    break;
                case "4":
                    userController.logout();     // 로그아웃
                    break;
                case "5":
                    System.out.println("프로그램 종료");
                    System.exit(0);
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }
}