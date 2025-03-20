package com.increff.model;

import com.increff.model.enums.Role;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData {
    private Long id;
    private String email;
    private Role role;
    private ZonedDateTime createdAt;
} 