// package com.increff.flow;

// import com.increff.entity.OrderEntity;
// import com.increff.entity.OrderItemEntity;
// import com.increff.entity.ProductEntity;
// import com.increff.model.enums.OrderStatus;
// import com.increff.model.orders.OrderData;
// import com.increff.model.orders.OrderForm;
// import com.increff.model.orders.OrderItemForm;
// import com.increff.service.ApiException;
// import com.increff.service.InventoryService;
// import com.increff.service.OrderService;
// import com.increff.service.ProductService;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.runners.MockitoJUnitRunner;
// import org.springframework.web.client.RestTemplate;

// import java.math.BigDecimal;
// import java.time.ZonedDateTime;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import static org.mockito.Mockito.*;
// import static org.junit.Assert.*;

// @RunWith(MockitoJUnitRunner.class)
// public class OrderFlowTest {

//     @Mock
//     private OrderService orderService;

//     @Mock
//     private ProductService productService;

//     @Mock
//     private InventoryService inventoryService;

//     @Mock
//     private RestTemplate restTemplate;

//     @InjectMocks
//     private OrderFlow flow;

//     @Test
//     public void testCreateOrder() throws ApiException {
//         // Create OrderForm instead of OrderEntity
//         OrderForm form = new OrderForm();
//         List<OrderItemForm> items = new ArrayList<>();
        
//         OrderItemForm item = new OrderItemForm();
//         item.setBarcode("barcode1");
//         item.setQuantity(5);
//         item.setSellingPrice(new BigDecimal("100.00"));
//         items.add(item);
        
//         form.setItems(items);
        
//         // Call createOrder with OrderForm
//         OrderData result = flow.createOrder(form);
        
//         // Assert results
//         assertNotNull(result);
//         assertEquals(OrderStatus.CREATED, result.getStatus());
//         assertEquals(1, result.getItems().size());
//     }

//     @Test(expected = ApiException.class)
//     public void testCreateOrderWithInvalidProduct() throws ApiException {
//         OrderForm form = new OrderForm();
//         List<OrderItemForm> items = new ArrayList<>();
        
//         OrderItemForm item = new OrderItemForm();
//         item.setBarcode("invalid-barcode");
//         item.setQuantity(5);
//         item.setSellingPrice(new BigDecimal("100.00"));
//         items.add(item);
        
//         form.setItems(items);
        
//         flow.createOrder(form);
//     }

//     @Test(expected = ApiException.class)
//     public void testCreateOrderWithInsufficientInventory() throws ApiException {
//         OrderForm form = new OrderForm();
//         List<OrderItemForm> items = new ArrayList<>();
        
//         OrderItemForm item = new OrderItemForm();
//         item.setBarcode("barcode1");
//         item.setQuantity(999999); // Very large quantity
//         item.setSellingPrice(new BigDecimal("100.00"));
//         items.add(item);
        
//         form.setItems(items);
        
//         flow.createOrder(form);
//     }

//     private ProductEntity createProduct(Long id, String name, String barcode) {
//         ProductEntity product = new ProductEntity();
//         product.setId(id);
//         product.setName(name);
//         product.setBarcode(barcode);
//         product.setMrp(new BigDecimal("99.99"));
//         return product;
//     }

//     private OrderItemEntity createOrderItem(Long orderId, Long productId, int quantity, BigDecimal sellingPrice) {
//         OrderItemEntity item = new OrderItemEntity();
//         item.setOrderId(orderId);
//         item.setProductId(productId);
//         item.setQuantity(quantity);
//         item.setSellingPrice(sellingPrice);
//         return item;
//     }
// } 