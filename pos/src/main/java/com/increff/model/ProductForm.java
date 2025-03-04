package com.increff.model;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductForm {
    
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than 0")
    private BigDecimal mrp;

    private String clientName;  // Added for search
    private BigDecimal minMrp;  // Added for search
    private BigDecimal maxMrp;  // Added for search

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    // New getters/setters for search fields
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public BigDecimal getMinMrp() {
        return minMrp;
    }

    public void setMinMrp(BigDecimal minMrp) {
        this.minMrp = minMrp;
    }

    public BigDecimal getMaxMrp() {
        return maxMrp;
    }

    public void setMaxMrp(BigDecimal maxMrp) {
        this.maxMrp = maxMrp;
    }
} 