package com.increff.controller;

import com.increff.entity.UserEntity;
import com.increff.model.UserForm;
import com.increff.model.LoginForm;
import com.increff.model.UserData;
import com.increff.service.UserService;
import com.increff.service.ApiException;
import com.increff.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Api
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService service;

    @Autowired
    private AuthService authService;

    @ApiOperation(value = "Signup")
    @PostMapping("/signup")
    public UserData signup(@Valid @RequestBody UserForm form) throws ApiException {
        form.setRole(form.getRole().toUpperCase()); // Normalize role string
        UserEntity user = service.signup(form);
        return convert(user);
    }

    @ApiOperation(value = "Login")
    @PostMapping("/login")
    public UserData login(@Valid @RequestBody LoginForm form, HttpSession session) throws ApiException {
        try {
            UserData user = authService.login(form.getEmail(), form.getPassword());
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole().name());
            return user;
        } catch (Exception e) {
            throw new ApiException("Login failed: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Get Current User")
    @GetMapping("/user")
    public UserData getCurrentUser(HttpSession session) throws ApiException {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new ApiException("Not logged in");
        }
        return authService.getUserById(userId);
    }

    @ApiOperation(value = "Logout")
    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    private UserData convert(UserEntity user) {
        UserData data = new UserData();
        data.setId(user.getId());
        data.setEmail(user.getEmail());
        data.setRole(user.getRole());
        return data;
    }
} 