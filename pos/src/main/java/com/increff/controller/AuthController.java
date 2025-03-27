package com.increff.controller;

import com.increff.dto.UserDto;
import com.increff.model.users.UserForm;
import com.increff.model.users.UserData;
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
        return dto.login(form, session);
    }

    @ApiOperation(value = "Get Current User")
    @GetMapping("/user")
    public UserData getCurrentUser(HttpSession session) {
        return dto.getCurrentUser(session);
    }

    @ApiOperation(value = "Logout")
    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }
} 