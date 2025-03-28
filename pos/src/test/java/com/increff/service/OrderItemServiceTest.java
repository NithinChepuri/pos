package com.increff.service;

import com.increff.dao.OrderItemDao;
import com.increff.entity.OrderItemEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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
        // Given
        Long productId = 1L;
        when(dao.countByProductId(productId)).thenReturn(5L);
        
        // When
        boolean result = service.existsByProductId(productId);
        
        // Then
        assertTrue(result);
        verify(dao).countByProductId(productId);
    }

    @Test
    public void testExistsByProductIdWhenNotExists() {
        // Given
        Long productId = 1L;
        when(dao.countByProductId(productId)).thenReturn(0L);
        
        // When
        boolean result = service.existsByProductId(productId);
        
        // Then
        assertFalse(result);
        verify(dao).countByProductId(productId);
    }



    private OrderItemEntity createOrderItem(Long orderId, Long productId, int quantity) {
        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setSellingPrice(new BigDecimal("99.99"));
        return item;
    }
} 