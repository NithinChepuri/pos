package com.increff.controller;

import com.increff.dto.OrderDto;
import com.increff.model.OrderData;
import com.increff.model.OrderForm;
import com.increff.model.InvoiceData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderDto dto;

    @Autowired
    private OrderService service;

    @ApiOperation(value = "Create new order")
    @PostMapping
    public OrderData add(@RequestBody OrderForm form) throws ApiException {
        // Validate client ID
        if (form.getClientId() == null) {
            throw new ApiException("Client ID is required");
        }
        return dto.add(form);
    }

    @ApiOperation(value = "Get order by ID")
    @GetMapping("/{id}")
    public OrderData get(@PathVariable Long id) throws ApiException {
        return dto.get(id);
    }

    @ApiOperation(value = "Get all orders")
    @GetMapping
    public List<OrderData> getAll() {
        return dto.getAll();
    }

    @ApiOperation(value = "Get orders by date range")
    @GetMapping("/filter")
    public List<OrderData> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        return dto.getByDateRange(startDate, endDate);
    }

    @ApiOperation(value = "Get order items")
    @GetMapping("/{id}/items")
    public List<OrderData> getOrderItems(@PathVariable Long id) {
        return dto.getOrderItems(id);
    }

    @ApiOperation(value = "Get invoice data for an order")
    @GetMapping("/{id}/invoice-data")
    public InvoiceData getInvoiceData(@PathVariable Long id) throws ApiException {
        if (id == null) {
            throw new ApiException("Order ID is required");
        }
        return service.getInvoiceData(id);
    }

    @ApiOperation(value = "Generate invoice PDF")
    @PostMapping("/{id}/invoice")
    public ResponseEntity<String> generateInvoice(@PathVariable Long id) throws ApiException {
        service.generateInvoice(id);
        return ResponseEntity.ok("Invoice generated successfully");
    }
} 