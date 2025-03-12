package com.increff.dto;

import com.increff.entity.UserEntity;
import com.increff.model.UserData;
import com.increff.model.UserForm;
import com.increff.service.ApiException;
import com.increff.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDto {

    @Autowired
    private UserService service;

    public UserData signup(UserForm form) {
        UserEntity user = service.signup(form);
        return convert(user);
    }

    public UserData login(UserForm form) {
        UserEntity user = service.login(form);
        return convert(user);
    }

    public UserData get(Long id) {
        UserEntity user = service.get(id);
        return convert(user);
    }
    
    public UserData getCurrentUser(Long userId) {
        if (userId == null) {
            throw new ApiException("Not logged in");
        }
        return get(userId);
    }

    private UserData convert(UserEntity user) {
        UserData data = new UserData();
        data.setId(user.getId());
        data.setEmail(user.getEmail());
        data.setRole(user.getRole());
        return data;
    }
} 