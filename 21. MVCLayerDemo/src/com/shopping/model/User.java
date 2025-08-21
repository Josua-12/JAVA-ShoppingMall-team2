package com.shopping.model;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // protected로 변경하면 자식 클래스에서 직접 접근 가능합니다.
    protected String id;
    protected String password;
    protected String email;
    protected String name;
    protected Role role;

    private int balance; // 사용자 잔액은 일반 User에게만 의미가 있을 수 있습니다.

    public User() {
        this("", "", "", "");
    }

    public User(String id, String password, String email, String name) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = Role.USER; // 기본 역할은 USER
        this.balance = 10000; // 초기 잔액
    }

    // getter & setter (생략)
    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public int getBalance() { return balance; }

    public void setId(String id) { this.id = id; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setRole(Role role) { this.role = role; }
    public void setBalance(int balance) { this.balance = balance; }


    // 권한 관련 메서드 (기본 사용자 기준)
    public boolean canBrowseProducts() { return true; }
    public boolean canAddToCart() { return true; }
    public boolean canPlaceOrder() { return true; }
    public boolean canManageProducts() { return false; }
    public boolean canViewAllOrders() { return false; }
    public boolean canManageUsers() { return false; }

    // 잔액 관련 메서드
    public boolean hasEnoughBalance(int amount) { return balance >= amount; }
    public void deductBalance(int amount) {
        if (amount > balance) throw new IllegalStateException("잔액 부족");
        balance -= amount;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, role=%s, balance=%d]", id, name, role, balance);
    }
}