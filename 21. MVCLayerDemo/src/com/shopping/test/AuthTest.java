package com.shopping.test;

import java.io.File;

import com.shopping.model.Role;
import com.shopping.model.User;
import com.shopping.repository.UserRepository;
import com.shopping.service.AuthService;

public class AuthTest {

    public static void main(String[] args) {
        
        new File("data").mkdir();
        UserRepository userRepository = new UserRepository();
        AuthService authService = new AuthService(userRepository);
        
        System.out.println("=== 인증 서비스 테스트 ===\n");
        
        //테스트1 : 정상 회원가입
        System.out.println("1. 정상 회원가입 테스트");
        try {
            User user = authService.registerUser("testuser1", "pass1234", "testuser1@choongang.com", "테스트유저1");
            System.out.println("성공 : " + user.getName() + 
                             " (잔액: " + user.getBalance() + "원)");            
        } catch(Exception e) {
            System.out.println("실패 : " + e.getMessage());
        }
        
        //테스트2 : 중복 이메일
        System.out.println("\n2. 중복 이메일 테스트");
        try {
            authService.registerUser("testuser2", "pass5678", "testuser1@choongang.com", "테스트유저2");
            System.out.println("오류 : 중복 이메일이 허용됨!");
        } catch(Exception e) {
            System.out.println("정상 : 중복 이메일 거부됨 - " + e.getMessage());
        }

        //테스트3 : 로그인 테스트
        System.out.println("\n3. 로그인 테스트");
        try {
            Role role = authService.login("testuser1@choongang.com", "pass1234");
            User loginUser = authService.getCurrentUser();
            System.out.println("성공 : " + loginUser.getName() + "님 로그인 성공");
        } catch(Exception e) {
            System.out.println("실패 : " + e.getMessage());
        }
        
        //테스트4 : 잘못된 비밀번호 로그인
        System.out.println("\n4. 잘못된 비밀번호 로그인 테스트");
        try {
            authService.login("testuser1@choongang.com", "wrongpass");
            System.out.println("오류 : 잘못된 비밀번호로 로그인 성공!");
        } catch(Exception e) {
            System.out.println("정상 : 잘못된 비밀번호 거부됨 - " + e.getMessage());
        }
        
        //테스트5 : 존재하지 않는 사용자 로그인
        System.out.println("\n5. 존재하지 않는 사용자 로그인 테스트");
        try {
            authService.login("nouser@choongang.com", "pass1234");
            System.out.println("오류 : 존재하지 않는 사용자로 로그인 성공!");
        } catch(Exception e) {
            System.out.println("정상 : 존재하지 않는 사용자 거부됨 - " + e.getMessage());
        }
        
        //테스트6 : 로그아웃
        System.out.println("\n6. 로그아웃 테스트");
        authService.logout();
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("성공 : 로그아웃 완료");
        } else {
            System.out.println("실패 : 로그아웃되지 않음");
        }
        
        System.out.println("\n=== 인증 테스트 완료 ===");
    }
}