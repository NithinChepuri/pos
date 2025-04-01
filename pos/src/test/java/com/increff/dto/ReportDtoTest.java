package com.increff.dto;

import com.increff.model.SalesReportData;
import com.increff.model.SalesReportForm;
import com.increff.model.clients.ClientForm;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.model.products.ProductForm;
import com.increff.model.inventory.InventoryForm;
import com.increff.service.ApiException;
import com.increff.spring.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Transactional
public class ReportDtoTest extends AbstractUnitTest {

    @Autowired
    private ReportDto reportDto;

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
        // Create client
        ClientForm clientForm = createClientForm();
        clientId = clientDto.add(clientForm).getId();

        // Create product
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




    @Test(expected = ApiException.class)
    public void testGetSalesReportWithInvalidDateRange() throws ApiException {
        // Given
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(ZonedDateTime.now().minusDays(1));
        form.setEndDate(ZonedDateTime.now().minusDays(7));
        
        // When/Then
        reportDto.getSalesReport(form);
    }

    @Test(expected = ApiException.class)
    public void testGetSalesReportWithNullDates() throws ApiException {
        // Given
        SalesReportForm form = new SalesReportForm();
        
        // When/Then
        reportDto.getSalesReport(form);
    }

    private ClientForm createClientForm() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");
        return form;
    }

    private OrderData createAndProcessOrder() throws ApiException {
        OrderForm orderForm = new OrderForm();
        List<OrderItemForm> items = new ArrayList<>();
        
        OrderItemForm item = new OrderItemForm();
        item.setBarcode(productBarcode);
        item.setQuantity(5);
        item.setSellingPrice(new BigDecimal("99.99"));
        items.add(item);
        
        orderForm.setItems(items);
        return orderDto.add(orderForm);
    }
} 