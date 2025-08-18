package com.shopping.service;

import com.shopping.model.User;
import com.shopping.model.Role;
import com.shopping.repository.UserRepository;
import com.shopping.util.PasswordEncoder;

/**
 * 간단한 인증 서비스 (Admin 없이 User만 사용)
 */
public class AuthService {

    private final UserRepository userRepository;
    private User currentUser; // 간단한 세션 관리

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        // 초기 관리자 계정 시드 생성
        initializeDefaultAdmin();
    }

    /**
     * 일반 사용자 회원가입
     */
    public User registerUser(String id, String password, String email, String name) {
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("이미 사용 중인 이메일입니다: " + email);
        }

        if (userRepository.existsById(id)) {
            throw new RuntimeException("이미 사용 중인 ID입니다: " + id);
        }

        String hashedPw = PasswordEncoder.hash(password);
        User user = new User(id, hashedPw, email, name);
        user.setRole(Role.USER); // 일반 사용자로 설정

        return userRepository.save(user);
    }

    /**
     * 관리자 계정 등록 (User를 관리자로 설정)
     */
    public User registerAdmin(String id, String password, String email, String name) {
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("이미 사용 중인 이메일입니다: " + email);
        }

        if (userRepository.existsById(id)) {
            throw new RuntimeException("이미 사용 중인 ID입니다: " + id);
        }

        String hashedPw = PasswordEncoder.hash(password);
        User admin = new User(id, hashedPw, email, name);
        admin.setRole(Role.ADMIN); // 관리자로 설정

        return userRepository.save(admin);
    }

    /**
     * 로그인
     */
    public Role login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("해당 이메일의 계정을 찾을 수 없습니다.");
        }
        
        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        
        this.currentUser = user;
        return user.getUserRole(); // getUserRole() 사용!
    }

    /**
     * 로그아웃
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * 현재 로그인된 사용자
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 현재 역할
     */
    public Role getCurrentRole() {
        return currentUser != null ? currentUser.getUserRole() : null;
    }

    /**
     * 로그인 상태 확인
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * 초기 관리자 계정 생성
     */
    private void initializeDefaultAdmin() {
        if (userRepository.findByEmail("admin@shopping.com") == null) {
            try {
                registerAdmin("admin", "admin123", "admin@shopping.com", "시스템 관리자");
                System.out.println("기본 관리자 계정이 생성되었습니다.");
            } catch (Exception e) {
                System.err.println("기본 관리자 계정 생성 실패: " + e.getMessage());
            }
        }
    }
}