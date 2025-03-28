package com.increff.dao;

import com.increff.entity.OrderEntity;
import com.increff.model.enums.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderDaoTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<OrderEntity> query;

    @InjectMocks
    private OrderDao dao;

    @Before
    public void setUp() {
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(em.createQuery(anyString(), eq(OrderEntity.class))).thenReturn(query);
    }

    @Test
    public void testInsert() {
        // Arrange
        OrderEntity order = createOrder(1L);
        
        // Act
        dao.insert(order);
        
        // Assert
        verify(em).persist(order);
    }

    @Test
    public void testSelect() {
        // Arrange
        Long id = 1L;
        OrderEntity order = createOrder(id);
        when(em.find(OrderEntity.class, id)).thenReturn(order);
        
        // Act
        OrderEntity result = dao.select(id);
        
        // Assert
        assertEquals(order, result);
    }

    @Test
    public void testSelectAll() {
        // Arrange
        List<OrderEntity> expectedOrders = Arrays.asList(
            createOrder(1L),
            createOrder(2L)
        );
        when(query.getResultList()).thenReturn(expectedOrders);
        
        // Act
        List<OrderEntity> result = dao.selectAll(0, 10);
        
        // Assert
        assertEquals(expectedOrders, result);
    }

    @Test
    public void testSelectByDateRange() {
        // Arrange
        ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
        ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC);
        
        List<OrderEntity> expectedOrders = Arrays.asList(
            createOrder(1L),
            createOrder(2L)
        );
        
        when(query.getResultList()).thenReturn(expectedOrders);
        
        // Act
        List<OrderEntity> result = dao.selectByDateRange(startDate, endDate, 0, 10);
        
        // Assert
        assertEquals(expectedOrders, result);
        verify(query).setParameter("startDate", startDate);
        verify(query).setParameter("endDate", endDate);
    }

    @Test
    public void testUpdate() {
        // Arrange
        OrderEntity order = createOrder(1L);
        
        // Act
        dao.update(order);
        
        // Assert
        verify(em).merge(order);
    }

    private OrderEntity createOrder(Long id) {
        OrderEntity order = new OrderEntity();
        order.setId(id);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        order.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        order.setVersion(1);
        return order;
    }
} 