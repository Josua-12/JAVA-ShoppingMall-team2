package com.shopping.model;

/*
 * 사람을 나타내는 추상클래스
 * user와 manager의 공통 부모 클래스
 */
public abstract class Person {

	protected String id;		// 아이디
	protected String name;	    // 이름
	protected String phone;     // 전화번호
	
	
	public Person(String id, String name, String phone) {
		//super();
		this.id = id;
		this.name = name;
		this.phone = phone;
	}


	public String getId() {
		return id;
	}


	public String getName() {
		return name;
	}


	public String getPhone() {
		return phone;
	}
	
	/*
	 * 역할 ("회원", "사서")
	 */
	public abstract String getRole();


	@Override
	public String toString() {
		return String.format("ID : %s, 이름 : %s, 전화번호 : %s, 역할 : %s", id, name, phone, getRole());
	}
}



























