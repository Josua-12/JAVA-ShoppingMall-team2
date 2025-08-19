package com.shopping.service;

import com.shopping.model.Admin;
import com.shopping.model.User;
import com.shopping.repository.AdminRepository;
import com.shopping.repository.UserRepository;
import com.shopping.util.PasswordEncoder;


 // User와 Admin 계정을 모두 인증하는 서비스
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

 // 회원가입 (User)
    public User registerUser(String id, String password, String email, String name) throws Exception {
        validateRegistration(id, email); // 중복 검증
        String encodedPw = PasswordEncoder.hash(password); // 비번 암호화
        User user = new User(id, encodedPw, email, name);
        userRepository.save(user);
        return user;
    }

 // 회원가입 (Admin)
    public Admin registerAdmin(String id, String password, String email, String name) throws Exception {
        validateRegistration(id, email); // 중복 검증
        String encodedPw = PasswordEncoder.hash(password); // 비번 암호화
        Admin admin = new Admin(id, encodedPw, email, name);
        adminRepository.save(admin);
        return admin;
    }


    // 로그인 (User 또는 Admin)
    public Object login(String email, String password) throws Exception {
        // User 먼저 검색
        User user = userRepository.findByEmail(email);
        if (user != null && PasswordEncoder.matches(password, user.getPassword())) {
            loggedInUser = user;
            return user;
        }

        // Admin 검색
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null && PasswordEncoder.matches(password, admin.getPassword())) {
            loggedInUser = admin;
            return admin;
        }

        throw new Exception("이메일 또는 비밀번호가 잘못되었습니다.");
    }

    // 로그아웃
    public void logout() {
        loggedInUser = null;
    }

    // 로그인 상태 확인
    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

 // 기본 관리자 생성
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

    // 회원가입 검증
    private void validateRegistration(String id, String email) {
        if (userRepository.existsById(id) || adminRepository.existsById(id)) {
            throw new IllegalArgumentException("이미 사용 중인 ID입니다: " + id);
        }
        if (userRepository.existsByEmail(email) || adminRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + email);
        }
    }
}