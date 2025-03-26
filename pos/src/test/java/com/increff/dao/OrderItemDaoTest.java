// package com.increff.dao;

// import com.increff.entity.OrderItemEntity;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.runners.MockitoJUnitRunner;

// import javax.persistence.EntityManager;
// import javax.persistence.TypedQuery;
// import java.math.BigDecimal;
// import java.util.Arrays;
// import java.util.List;

// import static org.mockito.Mockito.*;
// import static org.junit.Assert.*;

// @RunWith(MockitoJUnitRunner.class)
// public class OrderItemDaoTest {

//     @Mock
//     private EntityManager em;

//     @Mock
//     private TypedQuery<OrderItemEntity> itemQuery;
    
//     @Mock
//     private TypedQuery<Long> countQuery;

//     @InjectMocks
//     private OrderItemDao dao;

//     @Test
//     public void testInsert() {
//         // Arrange
//         OrderItemEntity orderItem = createOrderItem(1L, 1L, 5);
        
//         // Act
//         dao.insert(orderItem);
        
//         // Assert
//         verify(em).persist(orderItem);
//     }

//     @Test
//     public void testSelectByOrderId() {
//         // Arrange
//         Long orderId = 1L;
//         List<OrderItemEntity> expectedItems = Arrays.asList(
//             createOrderItem(orderId, 1L, 5),
//             createOrderItem(orderId, 2L, 10)
//         );
        
//         when(em.createQuery(anyString(), eq(OrderItemEntity.class))).thenReturn(itemQuery);
//         when(itemQuery.setParameter("orderId", orderId)).thenReturn(itemQuery);
//         when(itemQuery.getResultList()).thenReturn(expectedItems);
        
//         // Act
//         List<OrderItemEntity> result = dao.selectByOrderId(orderId);
        
//         // Assert
//         assertEquals(expectedItems, result);
//         verify(itemQuery).setParameter("orderId", orderId);
//     }

//     @Test
//     public void testCountByProductId() {
//         // Arrange
//         Long productId = 1L;
//         Long expectedCount = 5L;
        
//         when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
//         when(countQuery.setParameter("productId", productId)).thenReturn(countQuery);
//         when(countQuery.getSingleResult()).thenReturn(expectedCount);
        
//         // Act
//         long result = dao.countByProductId(productId);
        
//         // Assert
//         assertEquals(expectedCount.longValue(), result);
//         verify(countQuery).setParameter("productId", productId);
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