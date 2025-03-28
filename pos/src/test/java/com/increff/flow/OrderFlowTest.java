package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.enums.OrderStatus;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        // Given
        OrderForm form = createOrderForm();
        ProductEntity product = createProduct(1L, "Test Product", "1234567890");
        OrderEntity order = createOrder(1L);
        
        when(productService.getByBarcode("1234567890")).thenReturn(product);
        when(inventoryService.checkInventory(product.getId(), 5L)).thenReturn(true);
        when(orderService.createOrder(any(OrderEntity.class))).thenReturn(order);
        when(orderService.getOrderItems(order.getId())).thenReturn(new ArrayList<>());

        // When
        OrderData result = flow.createOrder(form);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CREATED, result.getStatus());
        verify(orderService).createOrder(any(OrderEntity.class));
    }

    @Test(expected = ApiException.class)
    public void testCreateOrderWithInvalidProduct() throws ApiException {
        // Given
        OrderForm form = createOrderForm();
        when(productService.getByBarcode(anyString())).thenReturn(null);

        // When/Then
        flow.createOrder(form);
    }

    @Test(expected = ApiException.class)
    public void testCreateOrderWithInsufficientInventory() throws ApiException {
        // Given
        OrderForm form = createOrderForm();
        ProductEntity product = createProduct(1L, "Test Product", "1234567890");
        
        when(productService.getByBarcode("1234567890")).thenReturn(product);
        when(inventoryService.checkInventory(product.getId(), 5L)).thenReturn(false);

        // When/Then
        flow.createOrder(form);
    }

    @Test
    public void testGetOrder() throws ApiException {
        // Given
        Long orderId = 1L;
        OrderEntity order = createOrder(orderId);
        List<OrderItemEntity> items = Arrays.asList(
            createOrderItem(orderId, 1L, 5)
        );
        
        when(orderService.get(orderId)).thenReturn(order);
        when(orderService.getOrderItems(orderId)).thenReturn(items);
        when(productService.get(1L)).thenReturn(createProduct(1L, "Test Product", "1234567890"));

        // When
        OrderData result = flow.getOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertEquals(1, result.getItems().size());
    }

    private OrderForm createOrderForm() {
        OrderForm form = new OrderForm();
        List<OrderItemForm> items = new ArrayList<>();
        
        OrderItemForm item = new OrderItemForm();
        item.setBarcode("1234567890");
        item.setQuantity(5);
        item.setSellingPrice(new BigDecimal("99.99"));
        items.add(item);
        
        form.setItems(items);
        return form;
    }

    private ProductEntity createProduct(Long id, String name, String barcode) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        product.setMrp(new BigDecimal("99.99"));
        return product;
    }

    private OrderEntity createOrder(Long id) {
        OrderEntity order = new OrderEntity();
        order.setId(id);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        order.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
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