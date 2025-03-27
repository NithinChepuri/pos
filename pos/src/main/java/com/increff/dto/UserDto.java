package com.increff.dto;

import com.increff.entity.UserEntity;
import com.increff.model.users.UserData;
import com.increff.model.users.UserForm;
import com.increff.service.ApiException;
import com.increff.service.UserService;
import com.increff.model.Constants;
import com.increff.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpSession;

@Component
public class UserDto {

    @Autowired
    private UserService service;

    public UserData signup(UserForm form) {
        UserEntity user = service.signup(form);
        return ConversionUtil.convertUserEntityToData(user);
    }

    public UserData login(UserForm form, HttpSession session) {
        UserEntity user = service.login(form);
        UserData userData = ConversionUtil.convertUserEntityToData(user);
        
        // Store user data in session
        session.setAttribute(Constants.SESSION_USER_ID, userData.getId());
        session.setAttribute(Constants.SESSION_ROLE, userData.getRole());
        session.setAttribute(Constants.SESSION_LAST_CHECKED_TIME, System.currentTimeMillis());
        
        return userData;
    }

    public UserData get(Long id) {
        UserEntity user = service.get(id);
        return ConversionUtil.convertUserEntityToData(user);
    }
    
    public UserData getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(Constants.SESSION_USER_ID);
        return getCurrentUser(userId);
    }

    public UserData getCurrentUser(Long userId) {
        if (userId == null) {
            throw new ApiException("Not logged in");
        }
        return get(userId);
    }


} 