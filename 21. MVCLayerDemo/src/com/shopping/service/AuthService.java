package com.shopping.service;

import com.shopping.model.Admin;
import com.shopping.model.User;
import com.shopping.model.Role;
import com.shopping.repository.AdminRepository;
import com.shopping.repository.UserRepository;
import com.shopping.util.PasswordEncoder;

/**
 * User와 Admin 계정을 모두 인증하는 서비스
 * 역할: 회원가입/로그인/로그아웃, 계정 중복·비번 검증, 현재 사용자(Role) 반환
 */
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    
    private Object loggedInUser; // 현재 로그인한 객체
    
    public Object getLoggedInUser() {
        return loggedInUser;
    }

    public AuthService(UserRepository userRepo, AdminRepository adminRepo) {
        this.userRepository = userRepo;
        this.adminRepository = adminRepo;
        
        ensureDefaultAdmin(); // 기본 관리자 계정 생성 보장
    }

    /**
     * 회원가입 (User)
     */
    public User registerUser(String id, String password, String email, String name) throws Exception {
        validateRegistration(id, email); // 중복 검증
        validatePassword(password); // 비밀번호 검증
        String encodedPw = PasswordEncoder.hash(password); // 비번 암호화
        User user = new User(id, encodedPw, email, name);
        userRepository.save(user);
        System.out.println("새 사용자 등록: " + id);
        return user;
    }

    /**
     * 회원가입 (Admin)
     */
    public Admin registerAdmin(String id, String password, String email, String name) throws Exception {
        validateRegistration(id, email); // 중복 검증
        validatePassword(password); // 비밀번호 검증
        String encodedPw = PasswordEncoder.hash(password); // 비번 암호화
        Admin admin = new Admin(id, encodedPw, email, name);
        adminRepository.save(admin);
        System.out.println("새 관리자 등록: " + id);
        return admin;
    }

    /**
     * 로그인 (User 또는 Admin) - Role 반환
     * @param email 이메일
     * @param password 비밀번호
     * @return 로그인한 사용자의 Role (USER 또는 ADMIN)
     * @throws Exception 로그인 실패 시
     */
    public Role login(String email, String password) throws Exception {
        // User 먼저 검색
        User user = userRepository.findByEmail(email);
        if (user != null && PasswordEncoder.matches(password, user.getPassword())) {
            loggedInUser = user;
            System.out.println("사용자 로그인 성공: " + email);
            return Role.USER;
        }

        // Admin 검색
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null && PasswordEncoder.matches(password, admin.getPassword())) {
            loggedInUser = admin;
            System.out.println("관리자 로그인 성공: " + email);
            return Role.ADMIN;
        }

        throw new Exception("이메일 또는 비밀번호가 잘못되었습니다.");
    }

    /**
     * 로그아웃
     */
    public void logout() {
        if (loggedInUser != null) {
            String userInfo = getCurrentUserInfo();
            loggedInUser = null;
            System.out.println("로그아웃: " + userInfo);
        }
    }

    /**
     * 로그인 상태 확인
     */
    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    /**
     * 현재 로그인한 사용자의 Role 반환
     */
    public Role getCurrentUserRole() {
        if (loggedInUser == null) {
            return null;
        }
        return (loggedInUser instanceof Admin) ? Role.ADMIN : Role.USER;
    }

    /**
     * 현재 사용자가 관리자인지 확인
     */
    public boolean isCurrentUserAdmin() {
        return loggedInUser instanceof Admin;
    }

    /**
     * 현재 사용자가 일반 사용자인지 확인
     */
    public boolean isCurrentUserUser() {
        return loggedInUser instanceof User;
    }

    /**
     * 기본 관리자 생성
     */
    private void ensureDefaultAdmin() {
        if (adminRepository.count() == 0) {
            try {
                registerAdmin("admin", "admin123", "admin@shopping.com", "시스템 관리자");
                System.out.println("기본 관리자 계정이 생성되었습니다.");
            } catch (Exception e) {
                System.out.println("기본 관리자 생성 실패: " + e.getMessage());
            }
        }
    }

    /**
     * 회원가입 검증 (ID, 이메일 중복 체크)
     */
    private void validateRegistration(String id, String email) throws Exception {
        if (userRepository.existsById(id) || adminRepository.existsById(id)) {
            throw new Exception("이미 사용 중인 ID입니다: " + id);
        }
        if (userRepository.existsByEmail(email) || adminRepository.existsByEmail(email)) {
            throw new Exception("이미 사용 중인 이메일입니다: " + email);
        }
    }

    /**
     * 비밀번호 검증
     */
    private void validatePassword(String password) throws Exception {
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("비밀번호는 필수입니다.");
        }
        if (password.length() < 6) {
            throw new Exception("비밀번호는 최소 6자 이상이어야 합니다.");
        }
    }

    /**
     * 현재 사용자 정보 문자열 반환 (로깅용)
     */
    private String getCurrentUserInfo() {
        if (loggedInUser == null) {
            return "없음";
        }
        if (loggedInUser instanceof User) {
            User user = (User) loggedInUser;
            return "User(" + user.getId() + ", " + user.getEmail() + ")";
        } else if (loggedInUser instanceof Admin) {
            Admin admin = (Admin) loggedInUser;
            return "Admin(" + admin.getId() + ", " + admin.getEmail() + ")";
        }
        return loggedInUser.toString();
    }
}