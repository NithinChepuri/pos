package com.increff.model.products;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductForm {
    
    @NotBlank(message = "Name is required")
    private String name;

//    @Size(min = 0, max = 50, message = "Barcode should not be greater than 50")
    @NotBlank(message = "Barcode is required")
    private String barcode;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than 0")
    private BigDecimal mrp;


} 