package com.shopping.service;

import com.shopping.model.Role;
import com.shopping.exception.UnauthorizedException;

/**
 * 권한 체크를 공통화하는 서비스 클래스
 * 역할: 권한 체크를 중복 제거하고 재사용 가능하게 만듦
 * "관리자만 상품관리 허용" 같은 규칙을 한 곳에서 관리
 */
public class AuthorizationService {
    
    private final AuthService authService;
    
    public AuthorizationService(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * 로그인 여부 확인
     * @throws UnauthorizedException 로그인하지 않은 경우
     */
    public void assertLoggedIn() throws UnauthorizedException {
        if (!authService.isLoggedIn()) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
    }
    
    /**
     * 특정 역할로 로그인했는지 확인
     * @param requiredRole 필요한 역할
     * @throws UnauthorizedException 해당 역할이 아닌 경우
     */
    public void assertLoggedIn(Role requiredRole) throws UnauthorizedException {
        assertLoggedIn(); // 먼저 로그인 체크
        
        Role currentRole = authService.getCurrentUserRole();
        if (currentRole != requiredRole) {
            throw new UnauthorizedException("접근 권한이 없습니다. 필요한 권한: " + requiredRole);
        }
    }
    
    /**
     * 관리자 권한 확인
     * @throws UnauthorizedException 관리자가 아닌 경우
     */
    public void assertAdminRole() throws UnauthorizedException {
        assertLoggedIn(Role.ADMIN);
    }
    
    /**
     * 일반 사용자 권한 확인
     * @throws UnauthorizedException 일반 사용자가 아닌 경우
     */
    public void assertUserRole() throws UnauthorizedException {
        assertLoggedIn(Role.USER);
    }
    
    /**
     * 상품 관리 권한 확인 (관리자만 허용)
     * @throws UnauthorizedException 상품 관리 권한이 없는 경우
     */
    public void assertCanManageProducts() throws UnauthorizedException {
        assertLoggedIn();
        
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("상품 관리는 관리자만 가능합니다.");
        }
    }
    
    /**
     * 모든 주문 조회 권한 확인 (관리자만 허용)
     * @throws UnauthorizedException 모든 주문 조회 권한이 없는 경우
     */
    public void assertCanViewAllOrders() throws UnauthorizedException {
        assertLoggedIn();
        
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("모든 주문 조회는 관리자만 가능합니다.");
        }
    }
    
    /**
     * 사용자 관리 권한 확인 (관리자만 허용)
     * @throws UnauthorizedException 사용자 관리 권한이 없는 경우
     */
    public void assertCanManageUsers() throws UnauthorizedException {
        assertLoggedIn();
        
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("사용자 관리는 관리자만 가능합니다.");
        }
    }
    
    /**
     * 자신의 정보만 조회/수정할 수 있는지 확인
     * @param targetUserId 대상 사용자 ID
     * @throws UnauthorizedException 권한이 없는 경우
     */
    public void assertCanAccessUserData(String targetUserId) throws UnauthorizedException {
        assertLoggedIn();
        
        // 관리자는 모든 사용자 데이터 접근 가능
        if (authService.isCurrentUserAdmin()) {
            return;
        }
        
        // 일반 사용자는 자신의 데이터만 접근 가능
        Object currentUser = authService.getLoggedInUser();
        if (currentUser instanceof com.shopping.model.User) {
            com.shopping.model.User user = (com.shopping.model.User) currentUser;
            if (!user.getId().equals(targetUserId)) {
                throw new UnauthorizedException("자신의 정보만 접근할 수 있습니다.");
            }
        }
    }
    
    /**
     * 주문 접근 권한 확인
     * @param orderUserId 주문한 사용자 ID
     * @throws UnauthorizedException 권한이 없는 경우
     */
    public void assertCanAccessOrder(String orderUserId) throws UnauthorizedException {
        assertLoggedIn();
        
        // 관리자는 모든 주문 접근 가능
        if (authService.isCurrentUserAdmin()) {
            return;
        }
        
        // 일반 사용자는 자신의 주문만 접근 가능
        Object currentUser = authService.getLoggedInUser();
        if (currentUser instanceof com.shopping.model.User) {
            com.shopping.model.User user = (com.shopping.model.User) currentUser;
            if (!user.getId().equals(orderUserId)) {
                throw new UnauthorizedException("자신의 주문만 접근할 수 있습니다.");
            }
        }
    }
    
    /**
     * 카테고리 관리 권한 확인 (관리자만 허용)
     * @throws UnauthorizedException 카테고리 관리 권한이 없는 경우
     */
    public void assertCanManageCategories() throws UnauthorizedException {
        assertLoggedIn();
        
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("카테고리 관리는 관리자만 가능합니다.");
        }
    }
    
    /**
     * 시스템 설정 관리 권한 확인 (관리자만 허용)
     * @throws UnauthorizedException 시스템 설정 권한이 없는 경우
     */
    public void assertCanManageSystem() throws UnauthorizedException {
        assertLoggedIn();
        
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("시스템 설정은 관리자만 가능합니다.");
        }
    }
    
    /**
     * 현재 사용자가 특정 역할을 가지고 있는지 확인 (예외 발생 없음)
     * @param role 확인할 역할
     * @return 해당 역할을 가지고 있으면 true
     */
    public boolean hasRole(Role role) {
        try {
            assertLoggedIn(role);
            return true;
        } catch (UnauthorizedException e) {
            return false;
        }
    }
    
    /**
     * 현재 사용자가 관리자인지 확인 (예외 발생 없음)
     * @return 관리자면 true
     */
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
    
    /**
     * 현재 사용자가 일반 사용자인지 확인 (예외 발생 없음)
     * @return 일반 사용자면 true
     */
    public boolean isUser() {
        return hasRole(Role.USER);
    }
}