package com.shopping.test;

import java.io.File;

// Role은 login 메서드 반환 타입으로 더 이상 사용되지 않으므로 import문에서 제거해도 됩니다.
// import com.shopping.model.Role; 
import com.shopping.model.User;
import com.shopping.repository.UserRepository;
import com.shopping.service.AuthService;

public class AuthTest {

    public static void main(String[] args) {
        
        // 테스트 실행 전 기존 데이터 삭제 (반복 테스트 시 일관된 결과 보장)
        File dataDir = new File("data");
        if (dataDir.exists()) {
            for (File file : dataDir.listFiles()) {
                file.delete();
            }
        } else {
            dataDir.mkdir();
        }

        UserRepository userRepository = new UserRepository();
        AuthService authService = new AuthService(userRepository);
        
        System.out.println("=== 인증 서비스 테스트 ===\n");
        
        // 테스트 1: 정상 회원가입
        System.out.println("1. 정상 회원가입 테스트");
        try {
            User user = authService.registerUser("testuser1", "pass1234", "testuser1@choongang.com", "테스트유저1");
            // User의 toString() 메서드를 활용하여 출력
            System.out.println("성공: " + user);
        } catch(Exception e) {
            System.out.println("실패: " + e.getMessage());
        }
        
        // 테스트 2: 중복 이메일
        System.out.println("\n2. 중복 이메일 테스트");
        try {
            authService.registerUser("testuser2", "pass5678", "testuser1@choongang.com", "테스트유저2");
            System.out.println("오류: 중복 이메일이 허용됨!");
        } catch(Exception e) {
            System.out.println("정상: 중복 이메일 거부됨 - " + e.getMessage());
        }

        // 테스트 3: 로그인 테스트
        System.out.println("\n3. 로그인 테스트");
        try {
            // ## 수정된 부분 ##
            // login 메서드가 User 객체를 직접 반환하므로, 바로 받아서 사용합니다.
            User loginUser = authService.login("testuser1@choongang.com", "pass1234");
            System.out.println("성공: " + loginUser.getName() + "님 로그인 성공 (Role: " + loginUser.getRole() + ")");
        } catch(Exception e) {
            System.out.println("실패: " + e.getMessage());
        }
        
        // 테스트 4: 잘못된 비밀번호 로그인
        System.out.println("\n4. 잘못된 비밀번호 로그인 테스트");
        try {
            authService.login("testuser1@choongang.com", "wrongpass");
            System.out.println("오류: 잘못된 비밀번호로 로그인 성공!");
        } catch(Exception e) {
            System.out.println("정상: 잘못된 비밀번호 거부됨 - " + e.getMessage());
        }
        
        // 테스트 5: 존재하지 않는 사용자 로그인
        System.out.println("\n5. 존재하지 않는 사용자 로그인 테스트");
        try {
            authService.login("nouser@choongang.com", "pass1234");
            System.out.println("오류: 존재하지 않는 사용자로 로그인 성공!");
        } catch(Exception e) {
            System.out.println("정상: 존재하지 않는 사용자 거부됨 - " + e.getMessage());
        }
        
        // 테스트 6: 로그아웃
        // 로그아웃 전에 로그인 상태여야 하므로, 3번 테스트 성공 후 진행되는 것이 자연스럽습니다.
        System.out.println("\n6. 로그아웃 테스트");
        authService.logout();
        if (!authService.isLoggedIn()) {
            System.out.println("성공: 로그아웃 완료");
        } else {
            System.out.println("실패: 로그아웃되지 않음");
        }
        
        // ## 추가된 부분 ##
        // 테스트 7: 관리자 로그인 테스트
        System.out.println("\n7. 관리자 로그인 테스트");
        try {
            // AuthService 생성 시 기본 생성되는 관리자 계정으로 로그인
            User adminUser = authService.login("admin@shopping.com", "admin123");
            System.out.println("성공: " + adminUser.getName() + "님 로그인 성공 (Role: " + adminUser.getRole() + ")");

            // 관리자 권한 확인 (다형성 확인)
            if (adminUser.canManageProducts()) {
                System.out.println("확인: 상품 관리 권한이 있습니다.");
            } else {
                System.out.println("오류: 상품 관리 권한이 없습니다.");
            }
        } catch(Exception e) {
            System.out.println("실패: " + e.getMessage());
        }
        
        System.out.println("\n=== 인증 테스트 완료 ===");
    }
}