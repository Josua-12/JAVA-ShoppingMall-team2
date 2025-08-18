package com.shopping.model;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String password;
    private String email;
    private String name;
    private Role role; // Role.USER

    private double balance; // 사용자 잔액

    public User() {
        this("", "", "", "");
    }

    public User(String id, String password, String email, String name) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = Role.USER; // 기본 역할 USER
        this.balance = 10000.0; // 초기 잔액
    }

    // getter & setter
    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public double getBalance() { return balance; }

    public void setId(String id) { this.id = id; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setRole(Role role) { this.role = role; }
    public void setBalance(double balance) { this.balance = balance; }

    // 권한 관련 메서드
    public boolean canAddToCart() { return true; }
    public boolean canPlaceOrder() { return true; }
    public boolean canManageProducts() { return false; }
    public boolean canViewAllOrders() { return false; }
    public boolean canManageUsers() { return false; }

    public boolean hasEnoughBalance(double amount) { return balance >= amount; }
    public void deductBalance(double amount) {
        if (amount > balance) throw new IllegalStateException("잔액 부족");
        balance -= amount;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, role=%s, balance=%.2f]", id, name, role, balance);
    }
}
