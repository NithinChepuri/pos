package com.increff.employee.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.increff.employee.util.DateConverter;

public class InvoiceDetails {
    private Long orderId;
    private ZonedDateTime orderDate;
    private List<InvoiceItemDetails> items;
    private BigDecimal total;

    @JsonProperty("orderDate")
    private void unpackOrderDate(Map<String, Object> dateMap) {
        this.orderDate = DateConverter.convertToZonedDateTime(dateMap);
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public ZonedDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(ZonedDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<InvoiceItemDetails> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDetails> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
} 