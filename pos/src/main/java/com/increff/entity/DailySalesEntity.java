package com.increff.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "pos_day_sales",
    indexes = {
        @Index(name = "idx_daily_sales_date", columnList = "date")
    }
)
public class DailySalesEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private LocalDate date;
    
    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;
    
    @Column(name = "total_items", nullable = false)
    private Integer totalItems;
    
    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;
    
    @Column(name = "invoicedItemCount", nullable = false)
    private Integer invoicedItemCount;
    
    @Column(name = "invoicedOrderCount", nullable = false)
    private Integer invoicedOrderCount;
    
    @Column(name = "totalRevenue", nullable = false)
    private BigDecimal totalRevenueAlternate;
    
    @Column(name = "invoiced_item_count", nullable = false)
    private Integer invoicedItemCountAlternate;
    
    @Column(name = "invoiced_order_count", nullable = false)
    private Integer invoicedOrderCountAlternate;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    // Default constructor
    public DailySalesEntity() {}
    
    // Constructor for daily sales data
    public DailySalesEntity(LocalDate date, Integer totalOrders, Integer totalItems, BigDecimal totalRevenue,
                           Integer invoicedOrderCount, Integer invoicedItemCount) {
        this.date = date;
        this.totalOrders = totalOrders;
        this.totalItems = totalItems;
        this.totalRevenue = totalRevenue;
        this.totalRevenueAlternate = totalRevenue;
        this.invoicedOrderCount = invoicedOrderCount;
        this.invoicedOrderCountAlternate = invoicedOrderCount;
        this.invoicedItemCount = invoicedItemCount;
        this.invoicedItemCountAlternate = invoicedItemCount;
        this.createdAt = ZonedDateTime.now();
    }
} 