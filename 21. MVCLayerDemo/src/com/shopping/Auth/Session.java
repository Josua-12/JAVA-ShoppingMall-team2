package com.shopping.Auth;

import com.shopping.model.Role;
import com.shopping.model.User;


//로그인 상태 공유용
public class Session {  
    private String userId;   // null이면 비로그인
    private Role role;       // null이면 비로그인
    public User user;  // 로그인한 User 객체 보관

    public boolean isLoggedIn() { return userId != null && role != null; }
    public boolean isAdmin() { return role == Role.ADMIN; }
    public User getUser() { return user; }
    public String getUserId() { return userId; }
    public Role getRole() { return role; }
    public void login(String userId, Role role, User user) { this.userId = userId; this.role = role; this.user = user; }
    public void logout() { this.userId = null; this.role = null; this.user = null; }
}
