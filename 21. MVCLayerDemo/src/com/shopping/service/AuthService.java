package com.shopping.service;

import com.shopping.model.User;
import com.shopping.repository.UserRepository;
import com.shopping.util.PasswordEncoder;

public class AuthService {

    private final UserRepository userRepository;
    private User currentUser; // 간단한 세션 흉내

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 회원가입 (User만)
    public User registerUser(String id, String password, String email, String name) {
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("이미 사용 중인 이메일입니다: " + email);
        }

        String hashedPw = PasswordEncoder.hash(password);
        User user = new User(id, hashedPw, email, name);

        return userRepository.save(user);
    }

    // 로그인
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("해당 이메일의 계정을 찾을 수 없습니다.");
        }
        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        this.currentUser = user;
        return user;
    }

    // 로그아웃
    public void logout() {
        this.currentUser = null;
    }

    // 현재 로그인된 사용자
    public User getCurrentUser() {
        return currentUser;
    }
}
