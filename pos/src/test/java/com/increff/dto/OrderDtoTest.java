package com.increff.dto;

import com.increff.flow.OrderFlow;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.model.orders.OrderItemData;
import com.increff.model.enums.OrderStatus;
import com.increff.service.ApiException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderDtoTest {

    @Mock
    private OrderFlow orderFlow;

    @InjectMocks
    private OrderDto dto;

    @Test
    public void testAdd() throws ApiException {
        // Given
        OrderForm form = createOrderForm();
        OrderData expectedData = createOrderData();
        when(orderFlow.createOrder(form)).thenReturn(expectedData);

        // When
        OrderData result = dto.add(form);

        // Then
        assertNotNull(result);
        assertEquals(expectedData.getId(), result.getId());
        assertEquals(expectedData.getStatus(), result.getStatus());
        verify(orderFlow).createOrder(form);
    }

    @Test(expected = ApiException.class)
    public void testAddWithEmptyForm() throws ApiException {
        // Given
        OrderForm form = new OrderForm();

        // When/Then
        dto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testAddWithDuplicateItems() throws ApiException {
        // Given
        OrderForm form = new OrderForm();
        List<OrderItemForm> items = new ArrayList<>();
        
        OrderItemForm item1 = createOrderItemForm("1234567890", 5);
        OrderItemForm item2 = createOrderItemForm("1234567890", 3);
        items.add(item1);
        items.add(item2);
        
        form.setItems(items);

        // When/Then
        dto.add(form);
    }

    @Test
    public void testGetByDateRange() throws ApiException {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(1);
        List<OrderData> expectedOrders = Arrays.asList(createOrderData(), createOrderData());
        
        when(orderFlow.getOrdersByDateRange(any(ZonedDateTime.class), 
            any(ZonedDateTime.class), anyInt(), anyInt())).thenReturn(expectedOrders);

        // When
        List<OrderData> results = dto.getOrdersByDateRange(startDate, endDate, 0, 10);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test(expected = ApiException.class)
    public void testGetByDateRangeWithFutureEndDate() throws ApiException {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);

        // When/Then
        dto.getOrdersByDateRange(startDate, endDate, 0, 10);
    }

    @Test(expected = ApiException.class)
    public void testGetByDateRangeWithStartAfterEnd() throws ApiException {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().minusDays(7);

        // When/Then
        dto.getOrdersByDateRange(startDate, endDate, 0, 10);
    }

    private OrderForm createOrderForm() {
        OrderForm form = new OrderForm();
        List<OrderItemForm> items = new ArrayList<>();
        items.add(createOrderItemForm("1234567890", 5));
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

    private OrderData createOrderData() {
        OrderData data = new OrderData();
        data.setId(1L);
        data.setStatus(OrderStatus.CREATED);
        data.setCreatedAt(ZonedDateTime.now().minusDays(1));
        return data;
    }
} 