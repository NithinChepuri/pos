package com.increff.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchForm {
    // Fields for searching - all optional
    private String name;
    private String barcode;
    private Long clientId;
    private String clientName;
} 