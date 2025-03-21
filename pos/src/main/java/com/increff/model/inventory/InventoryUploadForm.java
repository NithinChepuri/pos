package com.increff.model.inventory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class InventoryUploadForm {
    private String barcode;  // We'll use barcode to identify product
    private String quantity;

    
} 