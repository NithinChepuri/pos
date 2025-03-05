package com.increff.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderForm {
    private Long clientId;
    private List<OrderItemForm> items;
    
    public List<OrderItemForm> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemForm> items) {
        this.items = items;
    }
} 