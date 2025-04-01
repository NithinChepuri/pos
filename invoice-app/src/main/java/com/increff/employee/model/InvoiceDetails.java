package com.increff.employee.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.increff.employee.util.DateConverter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceDetails {   
    private Long orderId;
    private ZonedDateTime orderDate;
    private List<InvoiceItemDetails> items;
    private BigDecimal total;

    @JsonProperty("orderDate")
    private void unpackOrderDate(Map<String, Object> dateMap) {
        this.orderDate = DateConverter.convertToZonedDateTime(dateMap);
    }

    
} 