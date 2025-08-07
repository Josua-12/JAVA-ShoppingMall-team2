package com.shopping.repository;

import java.util.List;

import com.shopping.model.User;
import com.shopping.persistence.FileManager;
import com.shopping.util.Constants;

/*
 * 사용자 데이터 접근 담당하는 Repository 클래스
 * CRUD 기능 제공 
 */
public class UserRepository {

	//파일명 상수
	private static final String FILE_NAME = Constants.USER_DATA_FILE;
	
	//사용자 저장
	public User save(User user) {
		// 기존 사용자 목록 조회
		List<User> users = FileManager.readFromFile(FILE_NAME);
		
		// 새 사용자 추가
		users.add(user);
		
		// 파일에 저장
		FileManager.writeToFile(FILE_NAME, users);
		
		return user;
	}

	public boolean existsById(String id) {
		
		return findById(id) != null;
	}

	//ID로 사용자 조회
	private User findById(String id) {
		List<User> users = FileManager.readFromFile(FILE_NAME);
				
		return users.stream()
				.filter(u -> u.getId().equals(id))
				.findFirst()
				.orElse(null);
	}
}












