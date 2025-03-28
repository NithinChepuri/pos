package com.increff.service;

import com.increff.dao.OrderDao;
import com.increff.dao.OrderItemDao;
import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.model.enums.OrderStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderItemDao orderItemDao;

    @InjectMocks
    private OrderService service;

    @Test
    public void testCreateOrder() {
        // Given
        OrderEntity order = new OrderEntity();
        
        // When
        OrderEntity result = service.createOrder(order);
        
        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(orderDao).insert(order);
    }

    @Test
    public void testAddOrderItem() {
        // Given
        OrderItemEntity item = createOrderItem(1L, 1L, 5);
        
        // When
        service.addOrderItem(item);
        
        // Then
        verify(orderItemDao).insert(item);
    }

    @Test
    public void testGet() {
        // Given
        Long id = 1L;
        OrderEntity expectedOrder = createOrder(id);
        when(orderDao.select(id)).thenReturn(expectedOrder);
        
        // When
        OrderEntity result = service.get(id);
        
        // Then
        assertEquals(expectedOrder, result);
    }

    @Test
    public void testGetAll() {
        // Given
        List<OrderEntity> expectedOrders = Arrays.asList(
            createOrder(1L),
            createOrder(2L)
        );
        when(orderDao.selectAll(0, 10)).thenReturn(expectedOrders);
        
        // When
        List<OrderEntity> result = service.getAll(0, 10);
        
        // Then
        assertEquals(expectedOrders, result);
    }

    @Test
    public void testUpdateStatus() {
        // Given
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.INVOICED;
        OrderEntity order = createOrder(orderId);
        when(orderDao.select(orderId)).thenReturn(order);
        
        // When
        service.updateStatus(orderId, newStatus);
        
        // Then
        assertEquals(newStatus, order.getStatus());
        verify(orderDao).update(order);
    }

    private OrderEntity createOrder(Long id) {
        OrderEntity order = new OrderEntity();
        order.setId(id);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        return order;
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