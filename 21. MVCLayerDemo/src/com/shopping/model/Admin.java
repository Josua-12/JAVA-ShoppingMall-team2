package com.shopping.model;

// 1. 'extends User'를 추가하여 상속 관계를 명시합니다.
public class Admin extends User {

    private static final long serialVersionUID = 2L; // serialVersionUID는 자식 클래스에서 고유하게 가질 수 있습니다.

    public Admin() {
        // 2. super()를 통해 부모 클래스의 기본 생성자를 호출합니다.
        super();
        // Admin의 역할은 ADMIN으로 설정합니다.
        this.role = Role.ADMIN;
    }

    public Admin(String id, String password, String email, String name) {
        // 3. super(...)를 통해 부모 클래스의 필드를 초기화하는 생성자를 호출합니다.
        super(id, password, email, name);
        // Admin의 역할은 ADMIN으로 설정합니다.
        this.role = Role.ADMIN;
        // 관리자는 잔액이 필요 없으므로 0으로 설정하거나, 부모의 로직을 그대로 둘 수 있습니다.
        // 여기서는 부모 생성자에서 설정된 값을 그대로 사용합니다.
    }

    // id, password, email, name 및 관련 getter/setter는
    // User 클래스로부터 상속받았으므로 작성할 필요가 없습니다.
    @Override
    public boolean canAddToCart() { return false; } // 관리자는 장바구니 사용 안 함

    @Override
    public boolean canPlaceOrder() { return false; } // 관리자는 주문 안 함

    @Override
    public boolean canManageProducts() { return true; } // 관리자는 상품 관리 가능

    @Override
    public boolean canViewAllOrders() { return true; } // 관리자는 모든 주문 조회 가능

    @Override
    public boolean canManageUsers() { return true; } // 관리자는 사용자 관리 가능
    
    // 관리자는 잔액 관련 기능이 필요 없으므로 재정의하여 막을 수 있습니다.
    @Override
    public boolean hasEnoughBalance(double amount) {
        return false;
    }

    @Override
    public void deductBalance(double amount) {
        // 관리자는 잔액 차감 기능이 없음을 명확히 합니다.
        throw new UnsupportedOperationException("관리자는 잔액 관련 기능을 사용할 수 없습니다.");
    }


    @Override
    public String toString() {
        // 관리자 정보에 맞게 toString() 재정의
        return String.format("Admin[id=%s, name=%s, role=%s]", id, name, role);
    }
}