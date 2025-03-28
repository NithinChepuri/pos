package com.increff.controller;

import com.increff.dto.OrderDto;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.invoice.InvoiceData;
import com.increff.model.orders.OrderItemData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import com.increff.service.ApiException;

import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderDto dto;

    @ApiOperation(value = "Create new order")
    @PostMapping
    public OrderData add(@RequestBody OrderForm form) throws ApiException {
        return dto.add(form);
    }

    @ApiOperation(value = "Get order by ID")
    @GetMapping("/{id}")
    public OrderData get(@PathVariable Long id) throws ApiException {
        return dto.get(id);
    }

    @ApiOperation(value = "Get all orders with pagination")
    @GetMapping
    public List<OrderData> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return dto.getAll(page, size);
    }

    @ApiOperation(value = "Get order items")
    @GetMapping("/{id}/items")
    public List<OrderItemData> getOrderItems(@PathVariable Long id) throws ApiException {
        return dto.getOrderItems(id);
    }

    @ApiOperation(value = "Get invoice data for an order")
    @GetMapping("/{id}/invoice-data")
    public InvoiceData getInvoiceData(@PathVariable Long id) throws ApiException {
        return dto.getInvoiceData(id);
    }

    @ApiOperation(value = "Generate invoice for an order")
    @PostMapping("/{id}/invoice")
    public ResponseEntity<Resource> generateInvoice(@PathVariable Long id) throws ApiException {
        return dto.generateAndCacheInvoice(id);
    }

    @ApiOperation(value = "Get orders by date range with pagination")
    @GetMapping("/filter/date")
    public List<OrderData> getOrdersByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ApiException {
        
        return dto.getOrdersByDateRange(startDate, endDate, page, size);
    }
} 