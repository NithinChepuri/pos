package com.increff.model;

import com.increff.entity.OrderStatus;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderData {
    private Long id;
    private Long clientId;
    private OrderStatus status;
    private ZonedDateTime createdAt;
    private String invoicePath;
    private List<OrderItemData> items;
} 