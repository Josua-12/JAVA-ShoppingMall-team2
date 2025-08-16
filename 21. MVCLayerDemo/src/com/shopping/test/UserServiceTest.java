package com.shopping.test;

import java.io.File;

import com.shopping.model.User;
import com.shopping.service.UserService;

public class UserServiceTest {

    public static void main(String[] args) {
        
        new File("data").mkdir();
        UserService userService = new UserService();
        
        System.out.println("=== 사용자 서비스 테스트 ===\n");
        
        //테스트1 : 정상 회원가입
        System.out.println("1. 사용자 정상 회원가입 테스트");
        try {
            User user = userService.register("testuser1", "pass1234", "testuser1@choongang.com", "테스트유저1");
            System.out.println("성공 : " + user.getName() + 
                             " (잔액: " + user.getBalance() + "원)");            
        } catch(Exception e) {
            System.out.println("실패 : " + e.getMessage());
        }
        
        //테스트2 : 중복 ID
        System.out.println("\n2. 중복 ID 테스트");
        try {
            userService.register("testuser1", "pass5678", "testuser2@choongang.com", "테스트유저2");
            System.out.println("오류 : 중복 ID가 허용됨!");
        } catch(Exception e) {
            System.out.println("정상 : 중복 ID 거부됨 - " + e.getMessage());
        }
        
        //테스트3 : 중복 이메일 (다른 ID로)
        System.out.println("\n3. 중복 이메일 테스트");
        try {
            userService.register("testuser2", "pass5678", "testuser1@choongang.com", "테스트유저2");
            System.out.println("오류 : 중복 이메일이 허용됨!");
        } catch(Exception e) {
            System.out.println("정상 : 중복 이메일 거부됨 - " + e.getMessage());
        }
        
        //테스트4 : 유효한 다른 사용자 등록
        System.out.println("\n4. 유효한 다른 사용자 등록 테스트");
        try {
            User user = userService.register("testuser3", "pass9012", "testuser3@choongang.com", "테스트유저3");
            System.out.println("성공 : " + user.getName() + 
                             " (잔액: " + user.getBalance() + "원)");            
        } catch(Exception e) {
            System.out.println("실패 : " + e.getMessage());
        }
        
        System.out.println("\n=== 사용자 서비스 테스트 완료 ===");
    }
}