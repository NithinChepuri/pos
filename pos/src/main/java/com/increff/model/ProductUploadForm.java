package com.increff.model;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUploadForm {
    private Long clientId;
    private String name;
    private String barcode;
    private String mrp;
} 