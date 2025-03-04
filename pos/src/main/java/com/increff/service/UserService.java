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
    public UserEntity signup(UserForm form) {
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
        
        // Assign role based on email domain
        if (email.endsWith("@supervisor.com")) {
            user.setRole(Role.SUPERVISOR);
        } else {
            user.setRole(Role.OPERATOR);
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
} 