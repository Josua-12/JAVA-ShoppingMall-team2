package com.shopping.model;

import java.io.Serializable;

/**
 * 사용자를 나타내는 클래스
 * Role enum으로 일반 사용자와 관리자를 구분
 * 상품 조회, 장바구니, 주문 및 관리 기능 제공
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
    
    // 역할 관련 메서드들
    public String getRoleName() {
        return role.name(); // "USER" 또는 "ADMIN" 반환
    }
    
    public Role getUserRole() {  // AuthService에서 사용하는 메서드명
        return role;
    }
    
    public void setRole(Role role) {  // Role 변경 메서드 추가
        this.role = role;
        // 관리자는 잔액 사용하지 않으므로 0으로 설정
        if (role == Role.ADMIN) {
            this.balance = 0.0;
        }
    }
    
    @Override
    public String getRole() {
        return role == Role.ADMIN ? "관리자" : "회원"; // Person 클래스의 추상 메서드 오버라이드
    }
    
    // 권한 메서드들 - Role에 따라 동적으로 변경
    public boolean canBrowseProducts() {
        return true; // 모든 역할이 상품 조회 가능
    }
    
    public boolean canAddToCart() {
        return role == Role.USER; // 일반 사용자만 장바구니 사용
    }
    
    public boolean canPlaceOrder() {
        return role == Role.USER; // 일반 사용자만 주문 가능
    }
    
    public boolean canManageProducts() {
        return role == Role.ADMIN; // 관리자만 상품 관리 가능
    }
    
    public boolean canViewAllOrders() {
        return role == Role.ADMIN; // 관리자만 전체 주문 조회 가능
    }
    
    public boolean canManageUsers() {
        return role == Role.ADMIN; // 관리자만 사용자 관리 가능
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
    
    // 사용자 전용 편의 메서드들 (일반 사용자만 사용)
    public boolean hasEnoughBalance(double amount) {
        return role == Role.USER && this.balance >= amount;
    }
    
    public void deductBalance(double amount) {
        if (role != Role.USER) {
            throw new IllegalStateException("관리자는 잔액을 사용할 수 없습니다.");
        }
        if (hasEnoughBalance(amount)) {
            this.balance -= amount;
        } else {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
    }
    
    public void addBalance(double amount) {
        if (role == Role.USER && amount > 0) {
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