package com.increff.dao;

import com.increff.entity.UserEntity;
import com.increff.model.enums.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserDaoTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<UserEntity> query;

    @InjectMocks
    private UserDao dao;

    @Test
    public void testInsert() {
        // Arrange
        UserEntity user = createUser("test@example.com", "password", Role.OPERATOR);
        
        // Act
        UserEntity result = dao.insert(user);
        
        // Assert
        verify(em).persist(user);
        assertEquals(user, result);
    }

    @Test
    public void testFindByEmailWhenExists() {
        // Arrange
        String email = "test@example.com";
        UserEntity user = createUser(email, "password", Role.OPERATOR);
        List<UserEntity> userList = Collections.singletonList(user); // List with one user

        when(em.createQuery(anyString(), eq(UserEntity.class))).thenReturn(query);
        when(query.setParameter("email", email.toLowerCase())).thenReturn(query);
        when(query.getResultList()).thenReturn(userList); // Mock getResultList to return the list with the user

        // Act
        UserEntity result = dao.findByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }


    @Test
    public void testFindByEmailWhenNotExists() {
        // Arrange
        String email = "nonexistent@example.com";
        
        when(em.createQuery(anyString(), eq(UserEntity.class))).thenReturn(query);
        when(query.setParameter("email", email.toLowerCase())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());
        
        // Act
        UserEntity result = dao.findByEmail(email);
        
        // Assert
        assertNull(result);
    }

    @Test
    public void testSelect() {
        // Arrange
        Long id = 1L;
        UserEntity user = createUser("test@example.com", "password", Role.OPERATOR);
        when(em.find(UserEntity.class, id)).thenReturn(user);
        
        // Act
        UserEntity result = dao.select(id);
        
        // Assert
        assertEquals(user, result);
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