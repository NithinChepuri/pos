package com.increff.model.orders;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
//TODO: learn and implement jackson serialization
@Getter
@Setter
public class OrderForm {
    private List<OrderItemForm> items;

    @Override
    public String toString() {
        return "OrderForm{" +
            "items=" + items +
            '}';
    }
} 