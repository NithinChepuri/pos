package com.increff.service;

import com.increff.dao.UserDao;
import com.increff.entity.UserEntity;
import com.increff.entity.UserEntity.Role;
import com.increff.model.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class UserService {

    @Autowired
    private UserDao dao;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserEntity signup(UserForm form) throws ApiException {
        // Convert to lowercase and trim
        String email = form.getEmail().toLowerCase().trim();
        
        // Check if user exists
        if (dao.findByEmail(email) != null) {
            throw new ApiException("User already exists");
        }
        
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setCreatedAt(ZonedDateTime.now());
        
        // Set role from form instead of email domain check
        try {
            UserEntity.Role userRole = UserEntity.Role.valueOf(form.getRole().toUpperCase());
            user.setRole(userRole);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid role: " + form.getRole());
        }
        
        return dao.insert(user);
    }

    @Transactional(readOnly = true)
    public UserEntity login(UserForm form) {
        String email = form.getEmail().toLowerCase().trim();
        UserEntity user = dao.findByEmail(email);
        
        if (user == null || !passwordEncoder.matches(form.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid credentials");
        }
        
        return user;
    }

    @Transactional(readOnly = true)
    public UserEntity get(Long id) {
        UserEntity user = dao.select(id);
        if (user == null) {
            throw new ApiException("User not found");
        }
        return user;
    }

    public boolean isSupervisor(UserEntity user) {
        return user.getRole() == UserEntity.Role.SUPERVISOR;
    }
    
    public boolean isOperator(UserEntity user) {
        return user.getRole() == UserEntity.Role.OPERATOR;
    }
} 