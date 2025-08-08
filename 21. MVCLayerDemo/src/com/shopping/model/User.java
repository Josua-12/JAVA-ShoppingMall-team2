package com.shopping.model;

import java.io.Serializable;

/*
 * 사용자 정보를 담는 엔티티 클래스 
 * Serializable를 구현하여 파일 저장이 가능하도록 함 
 */

public class User extends Person implements Serializable {
	
	//직렬화 버전 UID (파일 저장/읽기 시 클래스 버전 관리)
	private static final long serialVersionID = 1L;
	
	private double balance;
	
	public User(String id, String password, String email, String name) {
		super(id, password, email, name);	// 부모(person)생성자 호출
		this.balance = 10000.0;	// 초기 잔액
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		if(balance >= 0) {
			this.balance = balance;
		} else {
			System.err.println("잔액은 음수가 될 수 없습니다.");
		}
	}

	@Override
	public String getRole() {
		return "회원";
	}	
}

















