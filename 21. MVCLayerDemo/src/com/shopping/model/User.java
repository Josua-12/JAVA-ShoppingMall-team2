package com.shopping.model;

import java.io.Serializable;

/**
 * 일반 사용자를 나타내는 클래스
 * Role.USER를 가지며, 일반 구매자의 권한만 허용
 * 상품 조회, 장바구니, 주문 중심의 행위만 가능
 */
public class User extends Person implements Serializable {
    
    // 직렬화 버전 UID
    private static final long serialVersionUID = 1L;
    
    // 필드
    private double balance;
    private Role role; // Role enum 사용
    
    // 기본 생성자
    public User() {
        super("", "", "", "");
        this.balance = 0.0;
        this.role = Role.USER; // 기본적으로 USER 역할
    }
    
    // 매개변수 생성자
    public User(String id, String password, String email, String name) {
        super(id, password, email, name); // 부모(Person) 생성자 호출
        this.balance = 10000.0; // 초기 잔액
        this.role = Role.USER; // USER 역할 설정
    }
    
    // 역할 관련 메서드
    public String getRoleName() {
        return "USER";
    }
    
    public Role getRoleEnum() {
        return role;
    }
    
    @Override
    public String getRole() {
        return "회원"; // Person 클래스의 추상 메서드 오버라이드
    }
    
    // 권한 메서드들 - 일반 사용자 권한 설정
    public boolean canBrowseProducts() {
        return true; // 상품 조회 허용
    }
    
    public boolean canAddToCart() {
        return true; // 장바구니 추가 허용
    }
    
    public boolean canPlaceOrder() {
        return true; // 주문 허용
    }
    
    public boolean canManageProducts() {
        return false; // 상품 관리 불허
    }
    
    public boolean canViewAllOrders() {
        return false; // 전체 주문 조회 불허
    }
    
    public boolean canManageUsers() {
        return false; // 사용자 관리 불허
    }
    
    // 기존 getter 메서드들
    public String getId() {
        return id;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public double getBalance() {
        return balance;
    }
    
    // 기존 setter 메서드들
    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            this.password = password;
        }
    }
    
    public void setBalance(double balance) {
        if (balance >= 0) {
            this.balance = balance;
        } else {
            System.err.println("잔액은 음수가 될 수 없습니다.");
        }
    }
    
    // 사용자 전용 편의 메서드들
    public boolean hasEnoughBalance(double amount) {
        return this.balance >= amount;
    }
    
    public void deductBalance(double amount) {
        if (hasEnoughBalance(amount)) {
            this.balance -= amount;
        } else {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
    }
    
    public void addBalance(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }
    
    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, role=%s, balance=%.2f]", 
                            id, name, getRoleName(), balance);
    }
    
    // 객체 동등성 비교 - ID 기준 (기존 코드 유지)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }
    
    // 해시코드 생성 - ID 기준 (기존 코드 유지)
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}