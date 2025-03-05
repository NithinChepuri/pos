package com.increff.employee.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.increff.employee.util.DateConverter;

public class InvoiceDetails {
    private Long orderId;
    private LocalDateTime orderDate;
    private List<InvoiceItemDetails> items;
    private BigDecimal totalAmount;

    @JsonProperty("orderDate")
    private void unpackOrderDate(Map<String, Object> dateMap) {
        this.orderDate = DateConverter.convertToLocalDateTime(dateMap);
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<InvoiceItemDetails> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDetails> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
} 