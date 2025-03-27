package com.increff.dto;

import com.increff.model.orders.*;
import com.increff.model.invoice.InvoiceData;
import com.increff.service.ApiException;
import com.increff.flow.OrderFlow;
import com.increff.spring.AppProperties;
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
        validateOrderForm(form);
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

    private void validateOrderForm(OrderForm form) throws ApiException {
        if (form == null || form.getItems() == null || form.getItems().isEmpty()) {
            throw new ApiException("Order must have at least one item");
        }

        // Check for duplicate barcodes
        Set<String> barcodes = new HashSet<>();
        for (OrderItemForm item : form.getItems()) {
            validateOrderItem(item);
            
            if (!barcodes.add(item.getBarcode())) {
                throw new ApiException("Duplicate product with barcode: " + item.getBarcode() + ". Please combine quantities instead.");
            }
        }
    }

    public void validateOrderItem(OrderItemForm item) throws ApiException {
        if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new ApiException("Quantity must be positive");
        }
        if (item.getSellingPrice() == null || item.getSellingPrice().doubleValue() <= 0) {
            throw new ApiException("Selling price must be positive");
        }
    }

    public List<OrderData> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) throws ApiException {
        // Convert LocalDate to ZonedDateTime with appropriate time components
        ZonedDateTime startDateTime = startDate != null ? 
            startDate.atStartOfDay(ZoneOffset.UTC) : null;
        
        ZonedDateTime endDateTime = endDate != null ? 
            endDate.atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC) : null;
        
        validateDateRange(startDateTime, endDateTime);
        return flow.getOrdersByDateRange(startDateTime, endDateTime, page, size);
    }



    private void validateDateRange(ZonedDateTime startDate, ZonedDateTime endDate) throws ApiException {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ApiException("Start date cannot be after end date");
        }
        
        if (startDate != null && startDate.isAfter(now)) {
            throw new ApiException("Start date cannot be in the future");
        }
        
        if (endDate != null && endDate.isAfter(now)) {
            throw new ApiException("End date cannot be in the future");
        }
        
        if (startDate != null && endDate != null) {
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 90) {
                throw new ApiException("Date range cannot exceed 3 months (90 days)");
            }
        }
    }
} 