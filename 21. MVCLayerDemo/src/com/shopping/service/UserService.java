package com.shopping.service;

import com.shopping.model.User;
import com.shopping.repository.UserRepository;

/*
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 	- 로그인 인증 처리
 * 	- 잔액 관리
 */
public class UserService {
	//데이터 접근을 위한 Repository
	private UserRepository userRepository;
	
	// UserService 생성자 : UserRepository 인스턴스 생성
	public UserService() {
		userRepository = new UserRepository();
	}
	
	/*
	 * 회원 가입 처리 - ID 중복불가, 패스워드 최소 4자리, 이름 필수, 초기 잔액 10000원 부여
	 */
	public User register(String id, String password, String name) {
		//비즈니스 로직1: ID 중복불가
		if(userRepository.existsById(id)) {
			throw new RuntimeException("이미 존재하는 ID입니다: " + id);
		}
		
		//비즈니스 로직2: 패스워드 최소 4자리 길이 체크
		if(password == null || password.length() < 4) {
			throw new RuntimeException("패스워드는 최소 4자리 이상이어야 합니다.");
		}
		
		//비즈니스 로직3: 이름 필수 입력 체크
		if(name == null || name.trim().isEmpty()) {
			throw new RuntimeException("이름을 입력해주세요.");
		}
		
		//비즈니스 로직4: 이름 길이 체크(2자 이상)
		if(name.trim().length() < 2) {
			throw new RuntimeException("이름은 2자 이상이어야 합니다.");
		}
				
		//User 객체 생성
		User user = new User(id, password, email, name.trim());
		
		//Repository 기능 호출
		User saveduser = userRepository.save(user);
		
		System.out.println("새 사용자 등록: " + saveduser.getId());
			
		return saveduser;
	}
	
}











