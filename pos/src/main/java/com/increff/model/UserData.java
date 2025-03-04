package com.increff.model;

import com.increff.entity.UserEntity.Role;

public class UserData {
    private Long id;
    private String email;
    private Role role;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
} 