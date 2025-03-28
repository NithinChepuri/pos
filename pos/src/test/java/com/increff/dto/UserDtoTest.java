package com.increff.dto;

import com.increff.entity.UserEntity;
import com.increff.model.enums.Role;
import com.increff.model.users.UserData;
import com.increff.model.users.UserForm;
import com.increff.service.ApiException;
import com.increff.service.UserService;
import com.increff.model.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserDtoTest {

    @Mock
    private UserService service;

    @InjectMocks
    private UserDto dto;

    @Test
    public void testSignup() {
        // Given
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("password");
        
        UserEntity user = createUser("test@example.com", "encoded_password", Role.OPERATOR);
        when(service.signup(form)).thenReturn(user);
        
        // When
        UserData result = dto.signup(form);
        
        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.OPERATOR, result.getRole());
    }

    @Test
    public void testLogin() {
        // Given
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("password");
        
        MockHttpSession session = new MockHttpSession();
        UserEntity user = createUser("test@example.com", "encoded_password", Role.OPERATOR);
        when(service.login(form)).thenReturn(user);
        
        // When
        UserData result = dto.login(form, session);
        
        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.OPERATOR, result.getRole());
        assertEquals(user.getId(), session.getAttribute(Constants.SESSION_USER_ID));
        assertEquals(user.getRole(), session.getAttribute(Constants.SESSION_ROLE));
    }

    @Test
    public void testGet() {
        // Given
        Long id = 1L;
        UserEntity user = createUser("test@example.com", "encoded_password", Role.OPERATOR);
        when(service.get(id)).thenReturn(user);
        
        // When
        UserData result = dto.get(id);
        
        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.OPERATOR, result.getRole());
    }

    @Test
    public void testGetCurrentUser() {
        // Given
        Long userId = 1L;
        UserEntity user = createUser("test@example.com", "encoded_password", Role.OPERATOR);
        when(service.get(userId)).thenReturn(user);
        
        // When
        UserData result = dto.getCurrentUser(userId);
        
        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.OPERATOR, result.getRole());
    }

    @Test(expected = ApiException.class)
    public void testGetCurrentUserWithNullId() {
        // When/Then - should throw ApiException
        dto.getCurrentUser((Long) null);
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