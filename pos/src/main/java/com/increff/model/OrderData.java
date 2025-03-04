package com.increff.model;

import com.increff.entity.OrderStatus;
import java.time.ZonedDateTime;
import java.util.List;

public class OrderData {
    private Long id;
    private ZonedDateTime createdAt;
    private OrderStatus status;
    private String invoicePath;
    private List<OrderItemData> items;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getInvoicePath() {
        return invoicePath;
    }
    
    public void setInvoicePath(String invoicePath) {
        this.invoicePath = invoicePath;
    }
    
    public List<OrderItemData> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemData> items) {
        this.items = items;
    }
} 