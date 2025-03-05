package com.increff.model;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class InvoiceData {
    private Long orderId;
    private LocalDateTime orderDate;
    private List<InvoiceItemData> items;
    private BigDecimal totalAmount;
} 