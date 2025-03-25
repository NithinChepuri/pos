package com.increff.service;

import com.increff.dao.UserDao;
import com.increff.entity.UserEntity;
import com.increff.model.enums.Role;
import com.increff.model.users.UserForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserDao dao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    @Test
    public void testSignupOperator() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("password");
        
        when(dao.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        
        UserEntity savedUser = createUser("test@example.com", "encoded_password", Role.OPERATOR);
        when(dao.insert(any(UserEntity.class))).thenReturn(savedUser);
        
        // Act
        UserEntity result = service.signup(form);
        
        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.OPERATOR, result.getRole());
        verify(passwordEncoder).encode("password");
    }

    @Test
    public void testSignupSupervisor() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("test@supervisor.com");
        form.setPassword("password");
        
        when(dao.findByEmail("test@supervisor.com")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        
        UserEntity savedUser = createUser("test@supervisor.com", "encoded_password", Role.SUPERVISOR);
        when(dao.insert(any(UserEntity.class))).thenReturn(savedUser);
        
        // Act
        UserEntity result = service.signup(form);
        
        // Assert
        assertNotNull(result);
        assertEquals("test@supervisor.com", result.getEmail());
        assertEquals(Role.SUPERVISOR, result.getRole());
    }

    @Test(expected = ApiException.class)
    public void testSignupUserAlreadyExists() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("existing@example.com");
        form.setPassword("password");
        
        UserEntity existingUser = createUser("existing@example.com", "encoded_password", Role.OPERATOR);
        when(dao.findByEmail("existing@example.com")).thenReturn(existingUser);
        
        // Act - should throw ApiException
        service.signup(form);
    }

    @Test
    public void testLoginSuccess() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("password");
        
        UserEntity user = createUser("test@example.com", "encoded_password", Role.OPERATOR);
        when(dao.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
        
        // Act
        UserEntity result = service.login(form);
        
        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test(expected = ApiException.class)
    public void testLoginUserNotFound() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("nonexistent@example.com");
        form.setPassword("password");
        
        when(dao.findByEmail("nonexistent@example.com")).thenReturn(null);
        
        // Act - should throw ApiException
        service.login(form);
    }

    @Test(expected = ApiException.class)
    public void testLoginInvalidPassword() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword("wrong_password");
        
        UserEntity user = createUser("test@example.com", "encoded_password", Role.OPERATOR);
        when(dao.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);
        
        // Act - should throw ApiException
        service.login(form);
    }

    @Test
    public void testGet() {
        // Arrange
        Long id = 1L;
        UserEntity user = createUser("test@example.com", "encoded_password", Role.OPERATOR);
        when(dao.select(id)).thenReturn(user);
        
        // Act
        UserEntity result = service.get(id);
        
        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test(expected = ApiException.class)
    public void testGetUserNotFound() {
        // Arrange
        Long id = 1L;
        when(dao.select(id)).thenReturn(null);
        
        // Act - should throw ApiException
        service.get(id);
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