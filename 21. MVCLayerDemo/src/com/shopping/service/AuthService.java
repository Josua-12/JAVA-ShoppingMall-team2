package com.shopping.service;

import com.shopping.model.Admin; // 1. Admin 클래스를 import 합니다.
import com.shopping.model.User;
import com.shopping.model.Role;
import com.shopping.repository.UserRepository;
import com.shopping.util.PasswordEncoder;

/**
 * User와 Admin 계정을 모두 인증하는 서비스
 */
public class AuthService {

    private final UserRepository userRepository;
    private User currentUser; // 현재 로그인한 사용자를 저장 (Admin도 User 타입으로 관리 가능)

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        initializeDefaultAdmin(); // 서버 시작 시 기본 관리자 계정 확인 및 생성
    }

    /**
     * 일반 사용자 회원가입
     */
    public User registerUser(String id, String password, String email, String name) {
        validateRegistration(id, email); // 중복 확인 로직 분리

        String hashedPw = PasswordEncoder.hash(password);
        User user = new User(id, hashedPw, email, name);
        // User 생성자는 기본적으로 role을 USER로 설정하므로 별도 set이 필요 없습니다.

        return userRepository.save(user);
    }

    /**
     * 관리자 계정 등록
     */
    public Admin registerAdmin(String id, String password, String email, String name) {
        validateRegistration(id, email); // 중복 확인 로직 분리

        String hashedPw = PasswordEncoder.hash(password);
        // 2. new User()가 아닌 new Admin()으로 Admin 객체를 직접 생성합니다.
        Admin admin = new Admin(id, hashedPw, email, name);
        // Admin 생성자는 기본적으로 role을 ADMIN으로 설정합니다.

        // UserRepository.save()는 User 타입을 받지만,
        // Admin은 User의 자식 클래스이므로 문제없이 저장됩니다. (다형성)
        userRepository.save(admin);
        return admin;
    }

    /**
     * 로그인
     * @return 로그인 성공 시 해당 User 또는 Admin 객체 반환, 실패 시 null 또는 예외 발생
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("해당 이메일의 계정을 찾을 수 없습니다.");
        }
        
        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        
        // 로그인 성공 시, currentUser에 사용자 정보 저장
        // user 변수에는 User 객체 또는 Admin 객체가 담겨있습니다.
        this.currentUser = user;
        return user; // 3. Role 대신 User 객체 자체를 반환하여 활용도를 높입니다.
    }

    /**
     * 로그아웃
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * 현재 로그인된 사용자 정보 반환
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 현재 로그인된 사용자의 역할 반환
     */
    public Role getCurrentRole() {
        // user.getRole()을 사용합니다. (이전 코드의 user.getUserRole()은 getRole()로 가정)
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * 로그인 상태 확인
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * ID, 이메일 중복 확인을 위한 private 헬퍼 메서드
     */
    private void validateRegistration(String id, String email) {
        if (userRepository.existsById(id)) {
            throw new RuntimeException("이미 사용 중인 ID입니다: " + id);
        }
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("이미 사용 중인 이메일입니다: " + email);
        }
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