package com.shopping.test;

import java.io.File;

import com.shopping.model.User;
import com.shopping.service.UserService;

public class RegisterTest {

	public static void main(String[] args) {
		
		new File("data").mkdir();
		UserService userService = new UserService();
		
		//테스트1 : 정상 회원가입
		System.out.println("1. 정상 회원가입 테스트");
		
		try {
			User user = userService.register("testuser1", "pass1234", "테스트유저1");
			System.out.println("성공 : " +user.getName()+ 
		                 			 "잔액: " +user.getBalance()+ "원)");			
		} catch(Exception e) {
			System.out.println("실패 :" + e.getMessage());
		}
		
		//테스트2 : 중복 ID
		try {
			userService.register("testuser1", "pass5678", "테스트유저2");
			System.out.println("오류 : 중복 ID가 허용됨!");
		}catch(Exception e) {
			System.out.println("정상 : 중복 ID 거부됨 - " + e.getMessage());
		}
	}
}
















