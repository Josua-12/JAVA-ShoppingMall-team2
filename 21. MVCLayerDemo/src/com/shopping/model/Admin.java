package com.shopping.model;

import java.io.Serializable;

/**
 * 관리자 정보를 담는 엔티티 클래스
 */
public class Admin implements Serializable, Role {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String password;
    private String email;
    private String name;

    /**
     * 기본 생성자 (직렬화를 위해 필요)
     */
    public Admin() {
        this("", "", "", "");
    }
    
    /**
     * 매개변수가 있는 생성자
     */
    public Admin(String id, String password, String email, String name) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.name = name;
    }

    // getter 메서드들
    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    
    // setter 메서드들
    public void setId(String id) { this.id = id; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }

    @Override
    public String getRoleName() {
        return "ADMIN";
    }
    
    @Override
    public String toString() {
        return String.format("Admin[id=%s, name=%s]", id, name);
    }
    
    /**
     * 객체 동등성 비교 (ID 기반)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Admin admin = (Admin) obj;
        return id != null && id.equals(admin.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}