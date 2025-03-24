package com.increff.model.sales;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class DailySalesData {
    private ZonedDateTime date;
    private Integer totalOrders;
    private Integer totalItems;
    private BigDecimal totalRevenue;
    private Integer invoicedOrderCount;
    private Integer invoicedItemCount;
    
    // Default constructor
    public DailySalesData() {}
    
    // Constructor for JPQL query
    public DailySalesData(ZonedDateTime date, Long orders, Long items, BigDecimal revenue) {
        this.date = date;
        this.totalOrders = orders != null ? orders.intValue() : 0;
        this.totalItems = items != null ? items.intValue() : 0;
        this.totalRevenue = revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public ZonedDateTime getDate() { return date; }
    public void setDate(ZonedDateTime date) { this.date = date; }
    
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public Integer getInvoicedOrderCount() { return invoicedOrderCount; }
    public void setInvoicedOrderCount(Integer invoicedOrderCount) { this.invoicedOrderCount = invoicedOrderCount; }
    
    public Integer getInvoicedItemCount() { return invoicedItemCount; }
    public void setInvoicedItemCount(Integer invoicedItemCount) { this.invoicedItemCount = invoicedItemCount; }
} 