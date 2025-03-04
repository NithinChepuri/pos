package com.increff.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping("/api/test")
public class TestController {

    @ApiOperation(value = "Test endpoint")
    @GetMapping
    public String test() {
        return "Swagger is working!";
    }
} 