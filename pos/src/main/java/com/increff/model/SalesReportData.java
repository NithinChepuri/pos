package com.increff.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SalesReportData {
    private String barcode;
    private String productName;
    private Integer quantity;
    private BigDecimal revenue;

    // Constructor for JPQL query
    public SalesReportData(String barcode, String productName, 
            Long quantity, BigDecimal revenue) {
        this.barcode = barcode;
        this.productName = productName;
        this.quantity = quantity != null ? quantity.intValue() : 0;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }

} 