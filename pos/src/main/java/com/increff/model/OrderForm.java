package com.increff.model;

import java.util.List;

public class OrderForm {
    private List<OrderItemForm> items;
    
    public List<OrderItemForm> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemForm> items) {
        this.items = items;
    }
} 