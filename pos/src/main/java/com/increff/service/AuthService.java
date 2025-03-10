package com.increff.service;

import com.increff.model.SignUpForm;
import com.increff.entity.UserEntity;
import com.increff.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.increff.service.ApiException;
import java.time.ZonedDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.increff.model.UserData;

@Service
public class AuthService {
    
    @Autowired
    private UserDao dao;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Transactional(rollbackFor = ApiException.class)
    public void signup(SignUpForm form) throws ApiException {
        // Validate role
        UserEntity.Role userRole;
        try {
            userRole = UserEntity.Role.valueOf(form.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid role selected");
        }
        
        String email = form.getEmail().toLowerCase();
        
        // Debug print
        System.out.println("Signup attempt - Email: " + email);
        System.out.println("Password being stored: " + form.getPassword());
        
        // Check if email already exists
        if (dao.findByEmail(email) != null) {
            throw new ApiException("User with this email already exists");
        }
        
        // Create new user
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(userRole);
        user.setCreatedAt(ZonedDateTime.now());
        
        dao.insert(user);
    }
    
    @Transactional(readOnly = true)
    public UserData login(String email, String password) throws ApiException {
        if (email == null || password == null) {
            throw new ApiException("Email and password cannot be empty");
        }
        
        UserEntity user = dao.findByEmail(email.toLowerCase());
        if (user == null) {
            throw new ApiException("Invalid credentials");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException("Invalid credentials");
        }
        
        return convert(user);
    }

    @Transactional(readOnly = true)
    public UserData getUserById(Long id) throws ApiException {
        UserEntity user = dao.select(id);
        if (user == null) {
            throw new ApiException("User not found");
        }
        return convert(user);
    }

    private UserData convert(UserEntity user) {
        UserData data = new UserData();
        data.setId(user.getId());
        data.setEmail(user.getEmail());
        data.setRole(user.getRole());
        return data;
    }
} 