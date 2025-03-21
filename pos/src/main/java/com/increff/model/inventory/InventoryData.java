package com.increff.model.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryData {
    private Long id;
    private Long productId;
    private Long quantity;
    
    // Add these fields for display purposes
    private String productName;
    private String barcode;
} 