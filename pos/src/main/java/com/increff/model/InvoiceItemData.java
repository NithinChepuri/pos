package com.increff.model;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceItemData {
    private String productName;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal totalPrice; // quantity * sellingPrice
} 