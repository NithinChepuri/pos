package com.increff.model;

import java.math.BigDecimal;

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

    // Getters
    public String getBarcode() { return barcode; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getRevenue() { return revenue; }
} 