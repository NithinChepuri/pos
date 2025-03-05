package com.increff.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class OrderItemForm {
    private String barcode;
    //change to long for quantity and add validations
    private Integer quantity;
    private BigDecimal sellingPrice;
} 