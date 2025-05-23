package com.increff.dto;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.model.orders.*;
import com.increff.model.invoice.InvoiceData;
import com.increff.service.ApiException;
import com.increff.flow.OrderFlow;
import com.increff.service.OrderService;
import com.increff.spring.AppProperties;
import com.increff.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

import java.time.ZonedDateTime;
import java.util.List;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
public class OrderDto {
    
    @Autowired
    private OrderFlow flow;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AppProperties appProperties;

    public OrderData add(OrderForm form) throws ApiException {
        ValidationUtil.validateOrderForm(form);
        return flow.createOrder(form);
    }
    
    public OrderData get(Long id) throws ApiException {
//        OrderEntity order = findOrderById(id);
//        return convertToOrderData(order);
        return flow.getOrder(id);
    }
    
    public List<OrderData> getAll(int page, int size) {
        return flow.getAllOrders(page, size);
    }
    

    
    public List<OrderItemData> getOrderItems(Long orderId) throws ApiException {
        return flow.getOrderItems(orderId);
    }
    


    public ResponseEntity<Resource> generateAndCacheInvoice(Long orderId) throws ApiException {
        return flow.generateAndCacheInvoice(orderId, appProperties.getInvoiceServiceUrl());
    }

    public List<OrderData> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) throws ApiException {
        ValidationUtil.validateDateRange(startDate,endDate);
        return flow.getOrdersByDateRange(startDate,endDate,page,size);
    }


} 