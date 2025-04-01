package com.increff.employee.model;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemData {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName; // From products table
    private Integer quantity;
    private Double sellingPrice;
    
    
} 