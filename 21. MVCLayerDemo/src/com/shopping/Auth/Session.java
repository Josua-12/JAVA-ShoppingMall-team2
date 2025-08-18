package com.shopping.Auth;

import com.shopping.model.Role;


//로그인 상태 공유용
public class Session {  
    private String userId;   // null이면 비로그인
    private Role role;       // null이면 비로그인

    public boolean isLoggedIn() { return userId != null && role != null; }
    public boolean isAdmin() { return role == Role.ADMIN; }

    public String getUserId() { return userId; }
    public Role getRole() { return role; }
    public void login(String userId, Role role) { this.userId = userId; this.role = role; }
    public void logout() { this.userId = null; this.role = null; }
}
