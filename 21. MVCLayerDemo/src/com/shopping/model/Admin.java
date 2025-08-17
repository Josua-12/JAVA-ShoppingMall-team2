package com.shopping.model;

import java.io.Serializable;

public class Admin implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String password;
    private String email;
    private String name;
    private Role role; // enum Role 사용
    
    public Admin() {
        this("", "", "", "");
    }
    
    public Admin(String id, String password, String email, String name) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = Role.ADMIN; // 기본 역할은 ADMIN
    }

    // getter & setter
    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Role getRole() { return role; }

    public void setId(String id) { this.id = id; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return String.format("Admin[id=%s, name=%s, role=%s]", id, name, role);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Admin admin = (Admin) obj;
        return id != null && id.equals(admin.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
