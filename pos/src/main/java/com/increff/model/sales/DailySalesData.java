package com.increff.model.sales;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    //todo remove if it is not used
    public DailySalesData(ZonedDateTime date, Long orders, Long items, BigDecimal revenue) {
        this.date = date;
        this.totalOrders = orders != null ? orders.intValue() : 0;
        this.totalItems = items != null ? items.intValue() : 0;
        this.totalRevenue = revenue != null ? revenue : BigDecimal.ZERO;
    }
}