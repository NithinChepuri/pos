package com.increff.dto;

import com.increff.entity.UserEntity;
import com.increff.model.enums.Role;
import com.increff.model.users.UserData;
import com.increff.model.users.UserForm;
import com.increff.service.ApiException;
import com.increff.service.UserService;
import com.increff.model.Constants;
import com.increff.spring.AbstractUnitTest;
import io.swagger.annotations.Api;
import org.h2.engine.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@Transactional
public class UserDtoTest extends AbstractUnitTest{

    @Autowired
    private UserDto dto;

    @Autowired
    private UserService service;

//    @Before

    @Test
    public void testSignup() throws ApiException{
        UserForm form  = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("password");
        UserEntity user  = createUser("test@example.com","encoded_password", Role.OPERATOR);
        // When
        UserData result = dto.signup(form);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.OPERATOR, result.getRole());

    }

    @Test
    public void testLogin() throws ApiException{
        UserForm form  = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("password");
        dto.signup(form);
        //trying to login
        UserForm loginform  = new UserForm();
        loginform.setEmail("test@example.com");
        loginform.setPassword("password");
        MockHttpSession session = new MockHttpSession();
        UserData data = dto.login(loginform,session);
        assertNotNull(data);
        assertEquals("test@example.com",data.getEmail());
        assertEquals(Role.OPERATOR,data.getRole());

    }

    @Test
    public void testGet(){
        Long id = 1L;
        UserForm form  = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("password");
        dto.signup(form);
        UserData data = dto.get(id);
        assertNotNull(data);
        assertEquals("test@example.com",data.getEmail());
        assertEquals(Role.OPERATOR,data.getRole());
    }

    @Test
    public void testGetCurrentUser() throws ApiException {
        // Given: Sign up a user
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("password");
        UserData result = dto.signup(form);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.SESSION_USER_ID, result.getId());
        UserData data = dto.getCurrentUser(session);
        assertNotNull(data);
        assertEquals("test@example.com", data.getEmail());
        assertEquals(Role.OPERATOR, data.getRole());
    }


    @Test(expected = ApiException.class)
    public  void testGetCurrentUserWithNullId(){
        dto.getCurrentUser((Long)null);
    }




    private UserEntity createUser(String email, String password, Role role) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setCreatedAt(ZonedDateTime.now());
        return user;
    }
} 