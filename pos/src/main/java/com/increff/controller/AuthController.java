package com.increff.controller;

import com.increff.entity.UserEntity;
import com.increff.model.UserForm;
import com.increff.model.UserData;
import com.increff.service.UserService;
import com.increff.service.ApiException;
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

    @ApiOperation(value = "Signup")
    @PostMapping("/signup")
    public UserData signup(@Valid @RequestBody UserForm form) {
        return convert(service.signup(form));
    }

    @ApiOperation(value = "Login")
    @PostMapping("/login")
    public UserData login(@Valid @RequestBody UserForm form, HttpSession session) {
        UserEntity user = service.login(form);
        // Store in session
        session.setAttribute("userId", user.getId());
        session.setAttribute("role", user.getRole());
        session.setAttribute("lastCheckedTime", System.currentTimeMillis());
        return convert(user);
    }

    @ApiOperation(value = "Get Current User")
    @GetMapping("/user")
    public UserData getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new ApiException("Not logged in");
        }
        return convert(service.get(userId));
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