package com.increff.model;

import com.increff.entity.UserEntity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData {
    private Long id;
    private String email;
    private Role role;
} 