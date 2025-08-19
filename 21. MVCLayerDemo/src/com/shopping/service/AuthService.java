package com.shopping.service;

import com.shopping.model.Admin; // 1. Admin 클래스를 import 합니다.
import com.shopping.model.User;
import com.shopping.model.Role;
import com.shopping.repository.AdminRepository;
import com.shopping.repository.UserRepository;
import com.shopping.util.PasswordEncoder;

/**
 * User와 Admin 계정을 모두 인증하는 서비스
 */
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private User currentUser;

    public AuthService(UserRepository userRepo, AdminRepository adminRepo) {
        this.userRepository = userRepo;
        this.adminRepository = adminRepo;
        ensureDefaultAdmin();
    }

    // 일반 사용자 회원가입
    public User registerUser(String id, String password, String email, String name) {
        validateRegistration(id, email);

        String hashedPw = PasswordEncoder.hash(password);
        User user = new User(id, hashedPw, email, name);

        return userRepository.save(user); // users.dat에 저장
    }

    // 관리자 계정 등록
    public Admin registerAdmin(String id, String password, String email, String name) {
        validateRegistration(id, email);

        String hashedPw = PasswordEncoder.hash(password);
        Admin admin = new Admin(id, hashedPw, email, name);

        return adminRepository.save(admin); // admins.dat에 저장
    }

    // 로그인
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = adminRepository.findByEmail(email); // 관리자도 체크
        }
        if (user == null) throw new IllegalArgumentException("해당 이메일의 계정을 찾을 수 없습니다.");

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        currentUser = user;
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    private void ensureDefaultAdmin() {
        if (adminRepository.count() == 0) {
            registerAdmin("admin", "admin123", "admin@shopping.com", "시스템 관리자");
            System.out.println("기본 관리자 계정이 생성되었습니다.");
        }
    }

    private void validateRegistration(String id, String email) {
        if (userRepository.existsById(id) || adminRepository.existsById(id)) {
            throw new IllegalArgumentException("이미 사용 중인 ID입니다: " + id);
        }
        if (userRepository.existsByEmail(email) || adminRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + email);
        }
    }
}
