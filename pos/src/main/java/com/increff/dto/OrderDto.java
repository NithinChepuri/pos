package com.increff.dto;

import com.increff.model.orders.*;
import com.increff.model.invoice.InvoiceData;
import com.increff.service.ApiException;
import com.increff.flow.OrderFlow;
import com.increff.spring.AppProperties;
import com.increff.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

import java.time.ZonedDateTime;
import java.util.List;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDate;

@Component
public class OrderDto {
    
    @Autowired
    private OrderFlow flow;

    @Autowired
    private AppProperties appProperties;

    public OrderData add(OrderForm form) throws ApiException {
        ValidationUtil.validateOrderForm(form);
        return flow.createOrder(form);
    }
    
    public OrderData get(Long id) throws ApiException {
        return flow.getOrder(id);
    }
    
    public List<OrderData> getAll(int page, int size) {
        return flow.getAllOrders(page, size);
    }
    
    public List<OrderData> getByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return flow.getOrdersByDateRange(startDate, endDate);
    }
    
    public List<OrderItemData> getOrderItems(Long orderId) throws ApiException {
        return flow.getOrderItems(orderId);
    }
    
    public void generateInvoice(Long orderId) throws ApiException {
        flow.generateInvoice(orderId);
    }

    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        return flow.getInvoiceData(orderId);
    }

    public ResponseEntity<Resource> generateAndCacheInvoice(Long orderId) throws ApiException {
        return flow.generateAndCacheInvoice(orderId, appProperties.getInvoiceServiceUrl());
    }
    //Try to remove this function todo
    public List<OrderData> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) throws ApiException {
        // Convert LocalDate to ZonedDateTime with appropriate time components
        ZonedDateTime startDateTime = startDate != null ? 
            startDate.atStartOfDay(ZoneOffset.UTC) : null;
        
        ZonedDateTime endDateTime = endDate != null ? 
            endDate.atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC) : null;
        
        ValidationUtil.validateDateRange(startDateTime, endDateTime);
        return flow.getOrdersByDateRange(startDateTime, endDateTime, page, size);
    }
} 