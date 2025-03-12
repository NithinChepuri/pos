package com.increff.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceData {
    private Long orderId;
    private ZonedDateTime orderDate;
    private List<InvoiceItemData> items = new ArrayList<>();
    private BigDecimal total;
} 