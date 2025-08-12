package com.shopping.model;

import java.io.Serializable;

/*
 * 사용자 정보를 담는 엔티티 클래스 
 * Serializable를 구현하여 파일 저장이 가능하도록 함 
 */

public class User extends Person implements Serializable {
	
	//직렬화 버전 UID (파일 저장/읽기 시 클래스 버전 관리)
	private static final long serialVersionID = 1L;
	
	//필드
	private double balance;
	
public User(String id, String password, String email, String name) {
		super(id, password, email, name);	// 부모(person)생성자 호출
		this.balance = 10000.0;	// 초기 잔액
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

	public String getEmail() {
		return email;
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

	@Override
	public String toString() {
		return String.format("User[id=%s, name=%s, balance=%.2f]", 
								id, name, balance);
	}


	
	/*
	 * 객체 동등성 비교
	 * 	- ID가 같으면 같은 사용자로 판단
	 */
	
	@Override
	public boolean equals(Object obj) {
		// 두 객체의 참조가 같으면(즉, 같은 인스턴스면) 바로 true를 반환함 
		// - 불필요한 비교 줄여서 성능 높임
		if(this == obj) {
			return true;
		}
	
		// 비교 대상 null이거나, 클래스 타입이 다르면 (즉 서로 다른 타입이면) false 반환
		// - 이는 User 타입끼리만 동등성 비교를 하겠다는 의미임
		if(obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		User user = (User)obj;	//다운캐스팅
		return id !=null && id.equals(user.id);
	}

	/*
	 * 해시코드 생성
	 * ID 기반으로 해시코드 생성
	 * - id가 null이 아니면 id의 해시코드를 반환하고, null이면 0을 반환함
	 * 
	 */
	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String getRole() {
		return "회원";
	}
	
	
	
}
















