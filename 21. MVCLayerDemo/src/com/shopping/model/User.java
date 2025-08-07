package com.shopping.model;

import java.io.Serializable;

/*
 * 사용자 정보를 담는 엔티티 클래스 
 * Serializable를 구현하여 파일 저장이 가능하도록 함 
 */

public class User implements Serializable {
	
	//직렬화 버전 UID (파일 저장/읽기 시 클래스 버전 관리)
	private static final long serialVersionID = 1L;
	
	//필드
	private String id;	//사용자 ID 
	private String password;
	private String name;
	private double balance;
	
	public User(String id, String password, String name) {
		//super();
		this.id = id;
		this.password = password;
		this.name = name;
		this.balance = 10000.0;		//초기 잔액 설정
	}

	public String getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public double getBalance() {
		return balance;
	}

	public void setPassword(String password) {
		if(password != null && !password.isEmpty())
			this.password = password;
	}

	public void setBalance(double balance) {
		if(balance >= 0) {
			this.balance = balance;
		} else {
			System.err.println("잔액은 음수가 될 수 없습니다.");
		}
	}
	
	
}

















