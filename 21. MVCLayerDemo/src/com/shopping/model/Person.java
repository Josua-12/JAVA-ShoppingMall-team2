package com.shopping.model;

/*
 * 사람을 나타내는 추상클래스
 * user와 manager의 공통 부모 클래스
 */
public abstract class Person {

	protected String id;		// 아이디
	protected String password;	// 비밀번호
	protected String email;		// 이메일
	protected String name;	    // 이름
	
	
	
	public Person(String id, String password, String email, String name) {
		//super();
		this.id = id;
		this.password = password;
		this.email = email;
		this.name = name;
	}

	public Person() {
		this("","","","");
	}
	
	public String getId() { return id; }
	public String getPassword() { return password; }
	public String getEmail() { return email; }
	public String getName() { return name; }
	
	/*
	 * 역할 ("회원", "관리자")
	 */
	public abstract String getRole();

	@Override
	public String toString() {
		return String.format("%s ID : %s, 비밀번호 : %s, 이메일 : %s, 이름 : %s", getRole(), id, password, email, name);
	}
}



























