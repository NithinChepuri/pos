package com.increff.dto;

import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.model.clients.ClientForm;
import com.increff.model.products.ProductForm;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.enums.OrderStatus;
import com.increff.service.ApiException;
import com.increff.spring.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Transactional
public class OrderDtoTest extends AbstractUnitTest {

    @Autowired
    private OrderDto orderDto;

    @Autowired
    private ClientDto clientDto;

    @Autowired
    private ProductDto productDto;

    @Autowired
    private InventoryDto inventoryDto;

    private Long clientId;
    private String productBarcode;
    private Long productId;

    @Before
    public void setUp() throws ApiException {
        // Create test client
        ClientForm clientForm = new ClientForm();
        clientForm.setName("Test Client");
        clientForm.setEmail("test@example.com");
        clientForm.setPhoneNumber("1234567890");
        clientId = clientDto.add(clientForm).getId();

        // Create test product
        ProductForm productForm = new ProductForm();
        productForm.setName("Test Product");
        productForm.setBarcode("1234567890");
        productForm.setMrp(new BigDecimal("99.99"));
        productForm.setClientId(clientId);
        productId = productDto.add(productForm).getId();
        productBarcode = productForm.getBarcode();

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setProductId(productId);
        inventoryForm.setQuantity(100L); // Add sufficient inventory
        inventoryDto.add(inventoryForm);
    }

    @Test
    public void testAdd() throws ApiException {
        // Given
        OrderForm form = createOrderForm(productBarcode, 5);

        // When
        OrderData result = orderDto.add(form);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertNotNull(result.getId());
    }

    @Test(expected = ApiException.class)
    public void testAddWithDuplicateItems() throws ApiException {
        // Given
        OrderForm form = new OrderForm();
        List<OrderItemForm> items = new ArrayList<>();
        
        OrderItemForm item1 = createOrderItemForm(productBarcode, 5);
        OrderItemForm item2 = createOrderItemForm(productBarcode, 3);
        items.add(item1);
        items.add(item2);
        
        form.setItems(items);

        // When/Then
        orderDto.add(form);
    }



    private OrderForm createOrderForm(String barcode, int quantity) {
        OrderForm form = new OrderForm();
        List<OrderItemForm> items = new ArrayList<>();
        items.add(createOrderItemForm(barcode, quantity));
        form.setItems(items);
        return form;
    }

    private OrderItemForm createOrderItemForm(String barcode, int quantity) {
        OrderItemForm item = new OrderItemForm();
        item.setBarcode(barcode);
        item.setQuantity(quantity);
        item.setSellingPrice(new BigDecimal("99.99"));
        return item;
    }
} 