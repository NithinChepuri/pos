package com.increff.model.products;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ProductSearchForm {
    // Fields for searching - all optional
    private String name;
    private String barcode;
    private Long clientId;
    private String clientName;
} 