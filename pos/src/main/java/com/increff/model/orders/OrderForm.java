package com.increff.model.orders;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
public class OrderForm {

    @Size(min=1, message = "Orders must have atleast 1 item")
    private List<OrderItemForm> items;

    @Override
    public String toString() {
        return "OrderForm{" +
            "items=" + items +
            '}';
    }
} 