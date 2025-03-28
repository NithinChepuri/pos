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
@Table(indexes = {
    @Index(name = "idx_daily_sales_date", columnList = "date")
})
public class DailySalesEntity extends AbstractEntity {
    //id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;
    
    @Column(nullable = false)
    private Integer totalOrders;
    
    @Column(nullable = false)
    private Integer totalItems;
    
    @Column(nullable = false)
    private BigDecimal totalRevenue;
    
    @Column(nullable = false)
    private Integer invoicedItemCount;
    
    @Column(nullable = false)
    private Integer invoicedOrderCount;
    
    // Constructor
    public DailySalesEntity() {}
    
    // Constructor for daily sales data
    public DailySalesEntity(LocalDate date, Integer totalOrders, Integer totalItems, BigDecimal totalRevenue,
                           Integer invoicedOrderCount, Integer invoicedItemCount) {
        this.date = date;
        this.totalOrders = totalOrders;
        this.totalItems = totalItems;
        this.totalRevenue = totalRevenue;
        this.invoicedOrderCount = invoicedOrderCount;
        this.invoicedItemCount = invoicedItemCount;
    }
} 