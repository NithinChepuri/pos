package com.increff.model.products;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductData {
    private Long id;
    private String name;
    private String barcode;
    private Long clientId;
    private BigDecimal mrp;
} 