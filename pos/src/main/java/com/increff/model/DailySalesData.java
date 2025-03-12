package com.increff.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class DailySalesData {
    private ZonedDateTime date;
    private Integer totalOrders;
    private Integer totalItems;
    private BigDecimal totalRevenue;
    
    // Constructor for JPQL query
    public DailySalesData(ZonedDateTime date, Long orders, Long items, BigDecimal revenue) {
        this.date = date;
        this.totalOrders = orders != null ? orders.intValue() : 0;
        this.totalItems = items != null ? items.intValue() : 0;
        this.totalRevenue = revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    // Getters
    public ZonedDateTime getDate() { return date; }
    public Integer getTotalOrders() { return totalOrders; }
    public Integer getTotalItems() { return totalItems; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
} 