package com.increff.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pos_day_sales")
public class DailySalesEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private ZonedDateTime date;
    
    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;
    
    @Column(name = "total_items", nullable = false)
    private Integer totalItems;
    
    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    // Default constructor
    public DailySalesEntity() {}
    
    // Constructor for JPQL query
    public DailySalesEntity(ZonedDateTime date, Long totalOrders, Long totalItems, BigDecimal totalRevenue) {
        this.date = date;
        this.totalOrders = totalOrders.intValue();
        this.totalItems = totalItems.intValue();
        this.totalRevenue = totalRevenue;
        this.createdAt = ZonedDateTime.now();
    }
} 