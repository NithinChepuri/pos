// package com.increff.service;

// import com.increff.dao.OrderDao;
// import com.increff.dao.OrderItemDao;
// import com.increff.entity.OrderEntity;
// import com.increff.entity.OrderItemEntity;
// import com.increff.model.enums.OrderStatus;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.runners.MockitoJUnitRunner;

// import java.math.BigDecimal;
// import java.time.ZonedDateTime;
// import java.time.ZoneOffset;
// import java.util.Arrays;
// import java.util.List;

// import static org.mockito.Mockito.*;
// import static org.junit.Assert.*;

// @RunWith(MockitoJUnitRunner.class)
// public class OrderServiceTest {

//     @Mock
//     private OrderDao orderDao;

//     @Mock
//     private OrderItemDao orderItemDao;

//     @InjectMocks
//     private OrderService service;

//     @Test
//     public void testCreateOrder() {
//         // Act
//         OrderEntity result = service.createOrder();
        
//         // Assert
//         assertNotNull(result);
//         assertEquals(OrderStatus.CREATED, result.getStatus());
//         assertNotNull(result.getCreatedAt());
//         verify(orderDao).insert(result);
//     }

//     @Test
//     public void testCreateOrderWithEntity() {
//         // Arrange
//         OrderEntity order = new OrderEntity();
        
//         // Act
//         OrderEntity result = service.createOrder(order);
        
//         // Assert
//         assertNotNull(result);
//         assertEquals(OrderStatus.CREATED, result.getStatus());
//         assertNotNull(result.getCreatedAt());
//         verify(orderDao).insert(order);
//     }

//     @Test
//     public void testCreateOrderWithPresetValues() {
//         // Arrange
//         OrderEntity order = new OrderEntity();
//         order.setStatus(OrderStatus.INVOICED);
//         ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
//         order.setCreatedAt(createdAt);
        
//         // Act
//         OrderEntity result = service.createOrder(order);
        
//         // Assert
//         assertNotNull(result);
//         assertEquals(OrderStatus.INVOICED, result.getStatus());
//         assertEquals(createdAt, result.getCreatedAt());
//         verify(orderDao).insert(order);
//     }

//     @Test
//     public void testAddOrderItem() {
//         // Arrange
//         OrderItemEntity item = createOrderItem(1L, 1L, 5);
        
//         // Act
//         service.addOrderItem(item);
        
//         // Assert
//         verify(orderItemDao).insert(item);
//     }

//     @Test
//     public void testGet() {
//         // Arrange
//         Long id = 1L;
//         OrderEntity expectedOrder = new OrderEntity();
//         expectedOrder.setId(id);
//         when(orderDao.select(id)).thenReturn(expectedOrder);
        
//         // Act
//         OrderEntity result = service.get(id);
        
//         // Assert
//         assertEquals(expectedOrder, result);
//     }

//     @Test
//     public void testGetAll() {
//         // Arrange
//         int page = 0;
//         int size = 10;
//         List<OrderEntity> expectedOrders = Arrays.asList(
//             createOrder(1L, OrderStatus.CREATED),
//             createOrder(2L, OrderStatus.INVOICED)
//         );
//         when(orderDao.selectAll(page, size)).thenReturn(expectedOrders);
        
//         // Act
//         List<OrderEntity> result = service.getAll(page, size);
        
//         // Assert
//         assertEquals(expectedOrders, result);
//     }

//     @Test
//     public void testGetOrderItems() {
//         // Arrange
//         Long orderId = 1L;
//         List<OrderItemEntity> expectedItems = Arrays.asList(
//             createOrderItem(orderId, 1L, 5),
//             createOrderItem(orderId, 2L, 10)
//         );
//         when(orderItemDao.selectByOrderId(orderId)).thenReturn(expectedItems);
        
//         // Act
//         List<OrderItemEntity> result = service.getOrderItems(orderId);
        
//         // Assert
//         assertEquals(expectedItems, result);
//     }

//     @Test
//     public void testUpdateStatus() {
//         // Arrange
//         Long orderId = 1L;
//         OrderStatus newStatus = OrderStatus.INVOICED;
//         OrderEntity order = createOrder(orderId, OrderStatus.CREATED);
//         when(orderDao.select(orderId)).thenReturn(order);
        
//         // Act
//         service.updateStatus(orderId, newStatus);
        
//         // Assert
//         assertEquals(newStatus, order.getStatus());
//         verify(orderDao).update(order);
//     }

//     @Test
//     public void testGetByDateRange() {
//         // Arrange
//         ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7);
//         ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC);
//         int page = 0;
//         int size = 10;
        
//         List<OrderEntity> expectedOrders = Arrays.asList(
//             createOrder(1L, OrderStatus.CREATED),
//             createOrder(2L, OrderStatus.INVOICED)
//         );
//         when(orderDao.selectByDateRange(startDate, endDate, page, size)).thenReturn(expectedOrders);
        
//         // Act
//         List<OrderEntity> result = service.getByDateRange(startDate, endDate, page, size);
        
//         // Assert
//         assertEquals(expectedOrders, result);
//         verify(orderDao).selectByDateRange(startDate, endDate, page, size);
//     }

//     private OrderEntity createOrder(Long id, OrderStatus status) {
//         OrderEntity order = new OrderEntity();
//         order.setId(id);
//         order.setStatus(status);
//         order.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
//         return order;
//     }

//     private OrderItemEntity createOrderItem(Long orderId, Long productId, int quantity) {
//         OrderItemEntity item = new OrderItemEntity();
//         item.setOrderId(orderId);
//         item.setProductId(productId);
//         item.setQuantity(quantity);
//         item.setSellingPrice(new BigDecimal("99.99"));
//         return item;
//     }
// } 