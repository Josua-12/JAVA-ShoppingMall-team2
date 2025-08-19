package com.shopping.test;

import java.io.File;

import com.shopping.model.User;
import com.shopping.model.Admin;
import com.shopping.repository.FileUserRepository;
import com.shopping.repository.FileAdminRepository;
import com.shopping.service.AuthService;

public class AuthTest {

    public static void main(String[] args) {

        // =======================
        // 0. 데이터 초기화
        // =======================
        File dataDir = new File("data");
        if (dataDir.exists()) {
            for (File file : dataDir.listFiles()) {
                file.delete();
            }
        } else {
            dataDir.mkdir();
        }
        System.out.println("데이터 초기화 완료!\n");

        // =======================
        // 1. 서비스 초기화
        // =======================
        FileUserRepository userRepo = new FileUserRepository();
        FileAdminRepository adminRepo = new FileAdminRepository();
        AuthService authService = new AuthService(userRepo, adminRepo);
        System.out.println("AuthService 초기화 완료!\n");

        // =======================
        // 2. User 관련 테스트
        // =======================
        System.out.println("===== User 테스트 시작 =====");

        // 2-1. 정상 회원가입
        System.out.println("\n[2-1] 정상 회원가입 테스트");
        try {
            User user = authService.registerUser("user1", "userpass", "user1@test.com", "사용자1");
            System.out.println("성공: " + user);
        } catch (Exception e) {
            System.out.println("실패: " + e.getMessage());
        }

        // 2-2. 중복 이메일 회원가입
        System.out.println("\n[2-2] 중복 이메일 회원가입 테스트");
        try {
            authService.registerUser("user2", "pass123", "user1@test.com", "사용자2");
            System.out.println("오류: 중복 이메일 허용됨!");
        } catch (Exception e) {
            System.out.println("정상: 중복 이메일 거부됨 - " + e.getMessage());
        }

        // 2-3. 로그인 테스트
        System.out.println("\n[2-3] 로그인 테스트");
        try {
            User loginUser = authService.login("user1@test.com", "userpass");
            System.out.println("성공: " + loginUser.getName() + "님 로그인 완료");
        } catch (Exception e) {
            System.out.println("실패: " + e.getMessage());
        }

        // 2-4. 잘못된 비밀번호
        System.out.println("\n[2-4] 잘못된 비밀번호 로그인 테스트");
        try {
            authService.login("user1@test.com", "wrongpass");
            System.out.println("오류: 잘못된 비밀번호로 로그인 성공!");
        } catch (Exception e) {
            System.out.println("정상: 로그인 거부됨 - " + e.getMessage());
        }

        // 2-5. 존재하지 않는 사용자
        System.out.println("\n[2-5] 존재하지 않는 사용자 로그인 테스트");
        try {
            authService.login("nouser@test.com", "pass123");
            System.out.println("오류: 존재하지 않는 사용자 로그인 성공!");
        } catch (Exception e) {
            System.out.println("정상: 로그인 거부됨 - " + e.getMessage());
        }

        // 2-6. 로그아웃
        System.out.println("\n[2-6] 로그아웃 테스트");
        authService.logout();
        System.out.println("로그아웃 상태: " + (authService.isLoggedIn() ? "실패" : "성공"));

        System.out.println("\n===== User 테스트 완료 =====\n");

        // =======================
        // 3. Admin 관련 테스트
        // =======================
        System.out.println("===== Admin 테스트 시작 =====");

        // 3-1. 기본 관리자 로그인
        System.out.println("\n[3-1] 기본 관리자 로그인 테스트");
        try {
            Admin admin = (Admin) authService.login("admin@shopping.com", "admin123");
            System.out.println("성공: " + admin.getName() + "님 로그인 완료 (Role: " + admin.getRole() + ")");
            System.out.println("관리 권한 확인: " + (admin.canManageProducts() ? "있음" : "없음"));
        } catch (Exception e) {
            System.out.println("실패: " + e.getMessage());
        }

        // 3-2. 관리자 회원가입 (추가)
        System.out.println("\n[3-2] 새로운 관리자 회원가입 테스트");
        try {
            Admin newAdmin = authService.registerAdmin("admin2", "adminpass", "admin2@test.com", "관리자2");
            System.out.println("성공: " + newAdmin.getName() + "님 관리자 등록 완료");
        } catch (Exception e) {
            System.out.println("실패: " + e.getMessage());
        }

        System.out.println("\n===== Admin 테스트 완료 =====\n");

        // =======================
        // 4. 파일 경로 확인
        // =======================
        System.out.println("users.dat 경로: " + new File("data/users.dat").getAbsolutePath());
        System.out.println("admins.dat 경로: " + new File("data/admins.dat").getAbsolutePath());

        System.out.println("\n=== 전체 테스트 완료 ===");
    }
}
