package com.increff.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemData {
    private String barcode;
    private String productName;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal total;
    
} 