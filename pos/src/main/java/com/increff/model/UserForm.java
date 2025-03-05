package com.increff.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserForm {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
} 