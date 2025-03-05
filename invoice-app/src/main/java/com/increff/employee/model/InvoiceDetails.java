package com.increff.employee.model;

import java.util.List;

public class InvoiceDetails {
    private OrderData order;
    private List<OrderItemData> orderItems;
    
    // Getters and Setters
    public OrderData getOrder() { return order; }
    public void setOrder(OrderData order) { this.order = order; }
    
    public List<OrderItemData> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemData> orderItems) { this.orderItems = orderItems; }
} 