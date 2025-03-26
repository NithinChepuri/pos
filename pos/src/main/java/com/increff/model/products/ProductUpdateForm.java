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
public class ProductUpdateForm {
    
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Barcode is required")
    @Size(min = 1, max = 50, message = "Barcode must be between 1 and 50 characters")
    private String barcode;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than 0")
    private BigDecimal mrp;
} 