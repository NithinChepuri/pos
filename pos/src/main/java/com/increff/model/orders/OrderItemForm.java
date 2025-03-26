package com.increff.model.orders;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class OrderItemForm {
    private String barcode;
    private Integer quantity;
    private BigDecimal sellingPrice;
} 