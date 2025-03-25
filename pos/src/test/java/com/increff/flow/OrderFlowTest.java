package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.enums.OrderStatus;
import com.increff.model.orders.OrderData;
import com.increff.service.ApiException;
import com.increff.service.InventoryService;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderFlowTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderFlow flow;

    @Test
    public void testCreateOrder() throws ApiException {
        // Arrange
        OrderEntity orderEntity = new OrderEntity();
        
        Map<String, OrderItemEntity> orderItemsMap = new HashMap<>();
        String barcode1 = "1234567890";
        String barcode2 = "0987654321";
        
        OrderItemEntity item1 = createOrderItem(null, null, 5, new BigDecimal("99.99"));
        OrderItemEntity item2 = createOrderItem(null, null, 3, new BigDecimal("49.99"));
        
        orderItemsMap.put(barcode1, item1);
        orderItemsMap.put(barcode2, item2);
        
        ProductEntity product1 = createProduct(1L, "Product 1", barcode1);
        ProductEntity product2 = createProduct(2L, "Product 2", barcode2);
        
        when(productService.getByBarcode(barcode1)).thenReturn(product1);
        when(productService.getByBarcode(barcode2)).thenReturn(product2);
        when(inventoryService.checkInventory(eq(1L), anyLong())).thenReturn(true);
        when(inventoryService.checkInventory(eq(2L), anyLong())).thenReturn(true);
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setStatus(OrderStatus.CREATED);
        savedOrder.setCreatedAt(ZonedDateTime.now());
        
        when(orderService.createOrder(any(OrderEntity.class))).thenReturn(savedOrder);
        
        // Act
        OrderData result = flow.createOrder(orderEntity, orderItemsMap);
        
        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        
        verify(orderService).createOrder(orderEntity);
        verify(orderService, times(2)).addOrderItem(any(OrderItemEntity.class));
        verify(inventoryService).checkInventory(eq(1L), anyLong());
        verify(inventoryService).checkInventory(eq(2L), anyLong());
    }

    @Test(expected = ApiException.class)
    public void testCreateOrderProductNotFound() throws ApiException {
        // Arrange
        OrderEntity orderEntity = new OrderEntity();
        
        Map<String, OrderItemEntity> orderItemsMap = new HashMap<>();
        String barcode = "1234567890";
        
        OrderItemEntity item = createOrderItem(null, null, 5, new BigDecimal("99.99"));
        orderItemsMap.put(barcode, item);
        
        when(productService.getByBarcode(barcode)).thenReturn(null);
        
        // Act - should throw ApiException
        flow.createOrder(orderEntity, orderItemsMap);
    }

    @Test(expected = ApiException.class)
    public void testCreateOrderInsufficientInventory() throws ApiException {
        // Arrange
        OrderEntity orderEntity = new OrderEntity();
        
        Map<String, OrderItemEntity> orderItemsMap = new HashMap<>();
        String barcode = "1234567890";
        
        OrderItemEntity item = createOrderItem(null, null, 5, new BigDecimal("99.99"));
        orderItemsMap.put(barcode, item);
        
        ProductEntity product = createProduct(1L, "Product 1", barcode);
        
        when(productService.getByBarcode(barcode)).thenReturn(product);
        when(inventoryService.checkInventory(eq(1L), anyLong())).thenReturn(false);
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setStatus(OrderStatus.CREATED);
        savedOrder.setCreatedAt(ZonedDateTime.now());
        
        when(orderService.createOrder(any(OrderEntity.class))).thenReturn(savedOrder);
        
        // Act - should throw ApiException
        flow.createOrder(orderEntity, orderItemsMap);
    }

    private ProductEntity createProduct(Long id, String name, String barcode) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        product.setMrp(new BigDecimal("99.99"));
        return product;
    }

    private OrderItemEntity createOrderItem(Long orderId, Long productId, int quantity, BigDecimal sellingPrice) {
        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setSellingPrice(sellingPrice);
        return item;
    }
} 