package com.increff.model.inventory;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceItemData {
    private String name;
    private String barcode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
} 