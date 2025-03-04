package com.increff.model;

public class InventoryUploadForm {
    private String barcode;  // We'll use barcode to identify product
    private String quantity;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
} 