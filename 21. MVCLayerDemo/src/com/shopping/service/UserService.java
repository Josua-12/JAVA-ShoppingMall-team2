package com.shopping.service;

import java.util.List;

import com.shopping.model.User;
import com.shopping.repository.FileUserRepository;
import com.shopping.util.PasswordEncoder;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
public class UserService {
    
    private final FileUserRepository fileUserRepository;
    
    public UserService() {
        this.fileUserRepository = new FileUserRepository();
    }
    
    public UserService(FileUserRepository fileUserRepository) {
        this.fileUserRepository = fileUserRepository;
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
        if (fileUserRepository.existsById(id)) {
            throw new Exception("이미 존재하는 ID입니다: " + id);
        }
        
        // 이메일 중복 체크
        if (fileUserRepository.findByEmail(email) != null) {
            throw new Exception("이미 사용 중인 이메일입니다: " + email);
        }
        
        // 비밀번호 해시화
        String hashedPassword = PasswordEncoder.hash(password);
        
        // 새 사용자 생성
        User user = new User(id, hashedPassword, email, name);
        
        // 저장 및 로그 출력
        User savedUser = fileUserRepository.save(user);
        System.out.println("새 사용자 등록: " + id);
        
        return savedUser;
    }
    
    /**
     * 사용자 조회 (ID)
     * @param id 사용자 ID
     * @return User 객체 (없으면 null)
     */
    public User findById(String id) {
        return fileUserRepository.findById(id);
    }
    
    /**
     * 사용자 조회 (이메일)
     * @param email 이메일
     * @return User 객체 (없으면 null)
     */
    public User findByEmail(String email) {
        return fileUserRepository.findByEmail(email);
    }

    public boolean deleteUser(String id) {
        return fileUserRepository.deleteById(id);
    }
    
    public List<User> findByName(String keyword) {
        return fileUserRepository.findByNameContaining(keyword);
    }

    public List<User> getAllUsers() {
        return fileUserRepository.findAll();
    }
    
    

    /**
     * 로그인 검증 (ID 기반)
     * @param id 사용자 ID
     * @param password 비밀번호
     * @return 로그인 성공한 User 객체
     * @throws Exception 로그인 실패 시
     */
    public User login(String id, String password) throws Exception {
        User user = fileUserRepository.findById(id);
        if (user == null) {
            throw new Exception("해당 ID의 사용자를 찾을 수 없습니다: " + id);
        }
        
        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new Exception("비밀번호가 올바르지 않습니다.");
        }
        
        return user;
    }
    
 // 개인정보 수정 기능
    public void updatePersonalInfo(String userId, String newName, String newEmail) throws Exception {
        // ID로 사용자 조회
        User user = fileUserRepository.findById(userId);
        if (user == null) {
            throw new Exception("사용자를 찾을 수 없습니다.");
        }

        // 유효성 검사
        if (newName == null || newName.trim().isEmpty()) {
            throw new Exception("이름을 올바르게 입력해주세요.");
        }
        if (newEmail == null || !newEmail.contains("@") || !newEmail.contains(".")) {
            throw new Exception("올바른 이메일 형식이 아닙니다.");
        }

        // 이메일 중복 검사 (본인 제외)
        User existingUser = fileUserRepository.findByEmail(newEmail);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new Exception("이미 사용 중인 이메일입니다.");
        }

        // 정보 업데이트
        user.setName(newName);
        user.setEmail(newEmail);

        // 변경사항 저장
        fileUserRepository.save(user);
    }
    
 // 비밀번호 변경 기능
    public void updatePassword(String userId, String newPassword) throws Exception {
        // 사용자 조회
        User user = fileUserRepository.findById(userId);
        if (user == null) {
            throw new Exception("사용자를 찾을 수 없습니다.");
        }

        // 비밀번호 유효성 검사 (간단 예시)
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new Exception("비밀번호를 올바르게 입력해주세요.");
        }

        // 비밀번호 해시화
        String hashedPassword = PasswordEncoder.hash(newPassword);

        // 비밀번호 변경
        user.setPassword(hashedPassword);

        // 변경사항 저장
        fileUserRepository.save(user);
    }

}
