package com.increff.controller;

import com.increff.dto.UserDto;
import com.increff.model.UserForm;
import com.increff.model.UserData;
import com.increff.model.Constants;
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
    private UserDto dto;

    @ApiOperation(value = "Signup")
    @PostMapping("/signup")
    public UserData signup(@Valid @RequestBody UserForm form) {
        return dto.signup(form);
    }

    @ApiOperation(value = "Login")
    @PostMapping("/login")
    public UserData login(@Valid @RequestBody UserForm form, HttpSession session) {
        UserData userData = dto.login(form);
        // Store in session
        session.setAttribute(Constants.SESSION_USER_ID, userData.getId());
        session.setAttribute(Constants.SESSION_ROLE, userData.getRole());
        session.setAttribute(Constants.SESSION_LAST_CHECKED_TIME, System.currentTimeMillis());
        return userData;
    }

    @ApiOperation(value = "Get Current User")
    @GetMapping("/user")
    public UserData getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(Constants.SESSION_USER_ID);
        return dto.getCurrentUser(userId);
    }

    @ApiOperation(value = "Logout")
    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }
} 