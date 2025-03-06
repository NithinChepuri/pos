package com.increff.model;

import java.util.List;

public class OrderForm {
    private Long clientId;
    private List<OrderItemForm> items;

    // Getters and Setters
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public List<OrderItemForm> getItems() {
        return items;
    }

    public void setItems(List<OrderItemForm> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "OrderForm{" +
            "clientId=" + clientId +
            ", items=" + items +
            '}';
    }
} 