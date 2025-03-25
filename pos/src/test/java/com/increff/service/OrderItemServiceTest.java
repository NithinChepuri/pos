package com.increff.service;

import com.increff.dao.OrderItemDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderItemServiceTest {

    @Mock
    private OrderItemDao dao;

    @InjectMocks
    private OrderItemService service;

    @Test
    public void testExistsByProductIdWhenExists() {
        // Arrange
        Long productId = 1L;
        when(dao.countByProductId(productId)).thenReturn(5L);
        
        // Act
        boolean result = service.existsByProductId(productId);
        
        // Assert
        assertTrue(result);
        verify(dao).countByProductId(productId);
    }

    @Test
    public void testExistsByProductIdWhenNotExists() {
        // Arrange
        Long productId = 1L;
        when(dao.countByProductId(productId)).thenReturn(0L);
        
        // Act
        boolean result = service.existsByProductId(productId);
        
        // Assert
        assertFalse(result);
        verify(dao).countByProductId(productId);
    }
} 