package com.shopping.util;

import java.io.File;

/*
 * 애플리케이션 전체에서 사용되는 상수 정의
 */
public class Constants {

	// 사용자 데이터 파일 
	public static final String USER_DATA_FILE = "data/users.dat";

	// 관리자 데이터 파일
	public static final String ADMIN_DATA_FILE = "data/admins.dat";
	
	// 카트 데이터 파일
	public static final String CART_DATA_FILE = "data/carts.dat";
	
	public static void main(String[] args) {
		System.out.println("Admin 파일 경로: " + new File(Constants.ADMIN_DATA_FILE).getAbsolutePath());
	}
	

}
