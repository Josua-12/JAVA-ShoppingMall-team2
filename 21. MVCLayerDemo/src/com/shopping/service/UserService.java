package com.shopping.service;

import com.shopping.model.User;
import com.shopping.repository.UserRepository;
import com.shopping.util.PasswordEncoder;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService() {
        this.userRepository = new UserRepository();
    }
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * 회원가입 (ID 기반)
     * @param id 사용자 ID
     * @param password 비밀번호
     * @param email 이메일
     * @param name 이름
     * @return 생성된 User 객체
     * @throws Exception 중복 ID나 이메일이 있을 경우
     */
    public User register(String id, String password, String email, String name) throws Exception {
        // ID 중복 체크
        if (userRepository.existsById(id)) {
            throw new Exception("이미 존재하는 ID입니다: " + id);
        }
        
        // 이메일 중복 체크
        if (userRepository.findByEmail(email) != null) {
            throw new Exception("이미 사용 중인 이메일입니다: " + email);
        }
        
        // 비밀번호 해시화
        String hashedPassword = PasswordEncoder.hash(password);
        
        // 새 사용자 생성
        User user = new User(id, hashedPassword, email, name);
        
        // 저장 및 로그 출력
        User savedUser = userRepository.save(user);
        System.out.println("새 사용자 등록: " + id);
        
        return savedUser;
    }
    
    /**
     * 사용자 조회 (ID)
     * @param id 사용자 ID
     * @return User 객체 (없으면 null)
     */
    public User findById(String id) {
        return userRepository.findById(id);
    }
    
    /**
     * 사용자 조회 (이메일)
     * @param email 이메일
     * @return User 객체 (없으면 null)
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * 로그인 검증 (ID 기반)
     * @param id 사용자 ID
     * @param password 비밀번호
     * @return 로그인 성공한 User 객체
     * @throws Exception 로그인 실패 시
     */
    public User login(String id, String password) throws Exception {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new Exception("해당 ID의 사용자를 찾을 수 없습니다: " + id);
        }
        
        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new Exception("비밀번호가 올바르지 않습니다.");
        }
        
        return user;
    }
}
