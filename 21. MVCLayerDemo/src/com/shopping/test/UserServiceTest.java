package com.shopping.test;

import com.shopping.model.User;
import com.shopping.model.Role;
import com.shopping.service.UserService;
import com.shopping.service.AuthService;
import com.shopping.repository.UserRepository;

/**
 * Shopping Mall 시스템 자동화 테스트 클래스
 * 각 기능을 체계적으로 테스트하고 결과를 출력
 */
public class UserServiceTest {
    
    private UserRepository userRepository;
    private UserService userService;
    private AuthService authService;
    
    // 테스트 통계
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    
    public UserServiceTest() {
        this.userRepository = new UserRepository();
        this.userService = new UserService(userRepository);
        this.authService = new AuthService(userRepository);
    }
    
    public static void main(String[] args) {
        UserServiceTest tester = new UserServiceTest();
        tester.runAllTests();
    }
    
    /**
     * 모든 테스트 실행
     */
    public void runAllTests() {
        printHeader("🛍️ Java Shopping Mall 시스템 테스트 시작");
        
        // 테스트 초기화
        setupTestData();
        
        // 테스트 실행
        testUserRegistration();
        testUserLogin();
        testUserInfoManagement();
        testAdminFunctionality();
        testErrorHandling();
        testSecurityFeatures();
        
        // 결과 출력
        printTestSummary();
    }
    
    /**
     * 테스트 데이터 초기화
     */
    private void setupTestData() {
        printSection("📋 테스트 환경 초기화");
        
        try {
            // 기존 데이터 정리 (실제 환경에서는 주의!)
            userRepository.deleteAll();
            
            // AuthService 초기화 시 기본 관리자 계정이 생성되는지 확인
            User admin = userRepository.findByEmail("admin@shopping.com");
            if (admin != null) {
                assertTest("기본 관리자 계정 생성", true, "✅ 기본 관리자 계정이 정상 생성됨");
            } else {
                assertTest("기본 관리자 계정 생성", false, "❌ 기본 관리자 계정 생성 실패");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 테스트 환경 초기화 실패: " + e.getMessage());
        }
    }
    
    /**
     * 시나리오 1: 회원가입 테스트
     */
    private void testUserRegistration() {
        printSection("👤 시나리오 1: 회원가입 테스트");
        
        // 1.1 정상 회원가입
        try {
            User user = authService.registerUser("testuser", "test1234", "testuser@test.com", "테스트유저");
            assertTest("정상 회원가입", user != null, "사용자: " + user.getName());
            assertTest("초기 잔액 설정", user.getBalance() == 10000.0, "잔액: " + user.getBalance() + "원");
            assertTest("사용자 역할 설정", user.getUserRole() == Role.USER, "역할: " + user.getRole());
        } catch (Exception e) {
            assertTest("정상 회원가입", false, "실패: " + e.getMessage());
        }
        
        // 1.2 중복 이메일 가입 시도
        try {
            authService.registerUser("testuser2", "test1234", "testuser@test.com", "중복유저");
            assertTest("중복 이메일 방지", false, "중복 가입이 허용됨 (문제!)");
        } catch (Exception e) {
            assertTest("중복 이메일 방지", true, "정상적으로 차단됨: " + e.getMessage());
        }
        
        // 1.3 중복 ID 가입 시도  
        try {
            authService.registerUser("testuser", "test1234", "testuser2@test.com", "중복ID유저");
            assertTest("중복 ID 방지", false, "중복 ID 가입이 허용됨 (문제!)");
        } catch (Exception e) {
            assertTest("중복 ID 방지", true, "정상적으로 차단됨: " + e.getMessage());
        }
    }
    
    /**
     * 시나리오 2: 로그인 테스트
     */
    private void testUserLogin() {
        printSection("🔐 시나리오 2: 로그인 테스트");
        
        // 2.1 정상 로그인 (일반 사용자)
        try {
            Role role = authService.login("testuser@test.com", "test1234");
            User currentUser = authService.getCurrentUser();
            
            assertTest("정상 로그인", currentUser != null, "로그인 사용자: " + currentUser.getName());
            assertTest("사용자 역할 확인", role == Role.USER, "역할: " + role);
            assertTest("현재 사용자 설정", currentUser.getEmail().equals("testuser@test.com"), "이메일 일치");
        } catch (Exception e) {
            assertTest("정상 로그인", false, "실패: " + e.getMessage());
        }
        
        // 2.2 잘못된 이메일로 로그인
        try {
            authService.logout(); // 먼저 로그아웃
            authService.login("notexist@test.com", "test1234");
            assertTest("존재하지 않는 이메일 로그인", false, "로그인이 허용됨 (문제!)");
        } catch (Exception e) {
            assertTest("존재하지 않는 이메일 로그인", true, "정상적으로 차단됨");
        }
        
        // 2.3 잘못된 패스워드로 로그인
        try {
            authService.login("testuser@test.com", "wrongpass");
            assertTest("잘못된 패스워드 로그인", false, "로그인이 허용됨 (문제!)");
        } catch (Exception e) {
            assertTest("잘못된 패스워드 로그인", true, "정상적으로 차단됨");
        }
        
        // 2.4 관리자 로그인
        try {
            authService.login("admin@shopping.com", "admin123");
            User admin = authService.getCurrentUser();
            Role adminRole = authService.getCurrentRole();
            
            assertTest("관리자 로그인", admin != null, "관리자: " + admin.getName());
            assertTest("관리자 역할 확인", adminRole == Role.ADMIN, "역할: " + adminRole);
            assertTest("관리자 잔액 확인", admin.getBalance() == 0.0, "잔액: " + admin.getBalance());
        } catch (Exception e) {
            assertTest("관리자 로그인", false, "실패: " + e.getMessage());
        }
    }
    
    /**
     * 시나리오 3: 사용자 정보 관리 테스트
     */
    private void testUserInfoManagement() {
        printSection("ℹ️ 시나리오 3: 사용자 정보 관리 테스트");
        
        // 3.1 로그인 상태에서 정보 조회
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            assertTest("로그인된 사용자 정보 조회", true, 
                String.format("ID: %s, 이름: %s, 이메일: %s", 
                    currentUser.getId(), currentUser.getName(), currentUser.getEmail()));
        } else {
            assertTest("로그인된 사용자 정보 조회", false, "현재 로그인된 사용자가 없음");
        }
        
        // 3.2 로그아웃 후 정보 조회 차단 테스트
        authService.logout();
        User afterLogout = authService.getCurrentUser();
        assertTest("로그아웃 후 현재 사용자", afterLogout == null, "로그아웃 상태 확인");
        
        // 3.3 UserService를 통한 사용자 조회
        User foundUser = userService.findByEmail("testuser@test.com");
        assertTest("이메일로 사용자 조회", foundUser != null, "사용자 찾기 성공");
        
        User foundById = userService.findById("testuser");
        assertTest("ID로 사용자 조회", foundById != null, "ID로 찾기 성공");
    }
    
    /**
     * 시나리오 4: 관리자 기능 테스트
     */
    private void testAdminFunctionality() {
        printSection("👑 시나리오 4: 관리자 기능 테스트");
        
        // 관리자로 로그인
        try {
            authService.login("admin@shopping.com", "admin123");
            User admin = authService.getCurrentUser();
            
            // 4.1 관리자 권한 확인
            assertTest("상품 조회 권한", admin.canBrowseProducts(), "모든 사용자 가능");
            assertTest("장바구니 권한", !admin.canAddToCart(), "관리자는 장바구니 사용 불가");
            assertTest("주문 권한", !admin.canPlaceOrder(), "관리자는 주문 불가");
            assertTest("상품 관리 권한", admin.canManageProducts(), "관리자만 가능");
            assertTest("전체 주문 조회 권한", admin.canViewAllOrders(), "관리자만 가능");
            assertTest("사용자 관리 권한", admin.canManageUsers(), "관리자만 가능");
            
            // 4.2 관리자 잔액 관리 테스트
            try {
                admin.deductBalance(1000);
                assertTest("관리자 잔액 차감", false, "관리자가 잔액을 사용함 (문제!)");
            } catch (IllegalStateException e) {
                assertTest("관리자 잔액 차감 방지", true, "정상적으로 차단됨");
            }
            
        } catch (Exception e) {
            assertTest("관리자 기능 테스트", false, "관리자 로그인 실패: " + e.getMessage());
        }
    }
    
    /**
     * 시나리오 5: 오류 처리 테스트
     */
    private void testErrorHandling() {
        printSection("⚠️ 시나리오 5: 오류 처리 테스트");
        
        // 5.1 비로그인 상태에서 로그아웃 시도
        authService.logout();
        User loggedOutUser = authService.getCurrentUser();
        assertTest("비로그인 상태 확인", loggedOutUser == null, "로그아웃 상태 정상");
        
        // 5.2 존재하지 않는 사용자 조회
        User notFound = userService.findById("notexistuser");
        assertTest("존재하지 않는 사용자 조회", notFound == null, "null 반환 정상");
        
        // 5.3 UserRepository 중복 검사
        boolean emailExists = userRepository.existsByEmail("testuser@test.com");
        assertTest("이메일 존재 확인", emailExists, "중복 검사 정상 동작");
        
        boolean emailNotExists = userRepository.existsByEmail("notexist@test.com");
        assertTest("존재하지 않는 이메일 확인", !emailNotExists, "존재하지 않음 확인");
    }
    
    /**
     * 시나리오 6: 보안 기능 테스트
     */
    private void testSecurityFeatures() {
        printSection("🔒 시나리오 6: 보안 기능 테스트");
        
        // 6.1 패스워드 해시화 확인
        try {
            User testUser = userRepository.findByEmail("testuser@test.com");
            if (testUser != null) {
                boolean isHashed = !testUser.getPassword().equals("test1234");
                assertTest("패스워드 해시화", isHashed, "패스워드가 해시화되어 저장됨");
                
                // 원본 패스워드와 다른지 확인
                assertTest("평문 패스워드 저장 방지", 
                    testUser.getPassword().length() > 10, 
                    "해시화된 패스워드 길이: " + testUser.getPassword().length());
            }
        } catch (Exception e) {
            assertTest("패스워드 해시화 확인", false, "테스트 실패: " + e.getMessage());
        }
        
        // 6.2 이메일 대소문자 구분 없는 조회
        User upperCaseEmail = userRepository.findByEmail("TESTUSER@TEST.COM");
        User lowerCaseEmail = userRepository.findByEmail("testuser@test.com");
        assertTest("이메일 대소문자 구분 없는 조회", 
            upperCaseEmail != null && lowerCaseEmail != null && 
            upperCaseEmail.getId().equals(lowerCaseEmail.getId()),
            "대소문자 구분 없이 동일 사용자 조회");
    }
    
    /**
     * 추가 통합 테스트
     */
    public void testCompleteUserJourney() {
        printSection("🚀 통합 테스트: 완전한 사용자 여정");
        
        try {
            // Step 1: 새 사용자 회원가입
            User newUser = authService.registerUser("journey", "pass1234", "journey@test.com", "여정테스트");
            assertTest("새 사용자 가입", newUser != null, "가입 성공");
            
            // Step 2: 로그인
            Role role = authService.login("journey@test.com", "pass1234");
            User loggedIn = authService.getCurrentUser();
            assertTest("신규 사용자 로그인", loggedIn != null && role == Role.USER, "로그인 성공");
            
            // Step 3: 사용자 권한 확인
            assertTest("일반 사용자 장바구니 권한", loggedIn.canAddToCart(), "장바구니 사용 가능");
            assertTest("일반 사용자 주문 권한", loggedIn.canPlaceOrder(), "주문 가능");
            assertTest("일반 사용자 관리 권한", !loggedIn.canManageProducts(), "상품 관리 불가");
            
            // Step 4: 잔액 관리
            double originalBalance = loggedIn.getBalance();
            assertTest("충분한 잔액 확인", loggedIn.hasEnoughBalance(5000), "5000원 결제 가능");
            
            loggedIn.deductBalance(3000);
            assertTest("잔액 차감", loggedIn.getBalance() == originalBalance - 3000, 
                "차감 후 잔액: " + loggedIn.getBalance());
            
            // Step 5: 로그아웃
            authService.logout();
            assertTest("사용자 여정 로그아웃", authService.getCurrentUser() == null, "로그아웃 완료");
            
        } catch (Exception e) {
            assertTest("완전한 사용자 여정", false, "실패: " + e.getMessage());
        }
    }
    
    /**
     * Repository 레이어 테스트
     */
    public void testRepositoryLayer() {
        printSection("🗄️ Repository 레이어 테스트");
        
        // 사용자 수 확인
        long userCount = userRepository.count();
        assertTest("사용자 수 조회", userCount >= 1, "등록된 사용자 수: " + userCount);
        
        // 전체 사용자 조회
        var allUsers = userRepository.findAll();
        assertTest("전체 사용자 조회", allUsers.size() >= 1, "조회된 사용자 수: " + allUsers.size());
        
        // ID로 존재 확인
        boolean exists = userRepository.existsById("testuser");
        assertTest("ID 존재 확인", exists, "testuser ID 존재함");
        
        boolean notExists = userRepository.existsById("notexistuser");
        assertTest("존재하지 않는 ID 확인", !notExists, "notexistuser ID 존재하지 않음");
    }
    
    // ============ 헬퍼 메서드들 ============
    
    /**
     * 테스트 결과 검증 및 출력
     */
    private void assertTest(String testName, boolean condition, String message) {
        totalTests++;
        System.out.printf("  %-30s ", testName + ":");
        
        if (condition) {
            System.out.println("✅ PASS - " + message);
            passedTests++;
        } else {
            System.out.println("❌ FAIL - " + message);
            failedTests++;
        }
    }
    
    /**
     * 섹션 헤더 출력
     */
    private void printSection(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(title);
        System.out.println("=".repeat(60));
    }
    
    /**
     * 전체 헤더 출력
     */
    private void printHeader(String title) {
        System.out.println("\n" + "╔" + "═".repeat(58) + "╗");
        System.out.println("║" + centerText(title, 58) + "║");
        System.out.println("╚" + "═".repeat(58) + "╝");
    }
    
    /**
     * 텍스트 중앙 정렬
     */
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
    
    /**
     * 테스트 결과 요약
     */
    private void printTestSummary() {
        System.out.println("\n" + "╔" + "═".repeat(58) + "╗");
        System.out.println("║" + centerText("🏁 테스트 결과 요약", 58) + "║");
        System.out.println("╠" + "═".repeat(58) + "╣");
        System.out.printf("║  총 테스트: %-10d통과: %-10d실패: %-10d  ║%n", totalTests, passedTests, failedTests);
        
        double successRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        System.out.printf("║  성공률: %.1f%%                                          ║%n", successRate);
        
        if (failedTests == 0) {
            System.out.println("║  🎉 모든 테스트가 성공했습니다!                        ║");
        } else {
            System.out.println("║  ⚠️  일부 테스트가 실패했습니다. 코드를 확인해주세요.   ║");
        }
        
        System.out.println("╚" + "═".repeat(58) + "╝");
        
        // 추가 통합 테스트 실행
        if (failedTests == 0) {
            System.out.println("\n🔄 추가 통합 테스트 실행...");
            testCompleteUserJourney();
            testRepositoryLayer();
            
            // 최종 결과
            System.out.println("\n" + "=".repeat(60));
            System.out.printf("최종 결과 - 총: %d, 성공: %d, 실패: %d (%.1f%%)%n", 
                totalTests, passedTests, failedTests, 
                totalTests > 0 ? (double) passedTests / totalTests * 100 : 0);
        }
    }
    
    /**
     * 실행 가능한 간단 테스트 메서드 (디버깅용)
     */
    public void quickTest() {
        System.out.println("🔧 빠른 테스트 실행...");
        
        try {
            // 기본 객체 생성 테스트
            System.out.println("UserRepository 생성: " + (userRepository != null ? "✅" : "❌"));
            System.out.println("UserService 생성: " + (userService != null ? "✅" : "❌"));
            System.out.println("AuthService 생성: " + (authService != null ? "✅" : "❌"));
            
            // 기본 관리자 계정 확인
            User admin = userRepository.findByEmail("admin@shopping.com");
            System.out.println("기본 관리자 계정: " + (admin != null ? "✅ 존재" : "❌ 없음"));
            
            if (admin != null) {
                System.out.println("  - 이름: " + admin.getName());
                System.out.println("  - 역할: " + admin.getRole());
                System.out.println("  - 잔액: " + admin.getBalance());
            }
            
        } catch (Exception e) {
            System.out.println("❌ 빠른 테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}