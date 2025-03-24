package com.increff.controller;

import com.increff.dto.OrderDto;
import com.increff.model.Properties;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.invoice.InvoiceData;
import com.increff.model.orders.OrderItemData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import com.increff.service.ApiException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Api
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderDto dto;

    @ApiOperation(value = "Create new order")
    @PostMapping
    //Todo: Restcontrolleradive implementaiton
    public ResponseEntity<?> add(@RequestBody OrderForm form) throws ApiException{
        logger.info("Creating order with form: " + form);
        try {
            OrderData orderData = dto.add(form);
            return ResponseEntity.ok(orderData);
        } catch (ApiException e) {
            logger.error("API Exception while creating order: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error while creating order: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ApiOperation(value = "Get order by ID")
    @GetMapping("/{id}")
    public OrderData get(@PathVariable Long id) {
        return dto.get(id);
    }

    @ApiOperation(value = "Get all orders with pagination")
    @GetMapping
    public List<OrderData> getAll(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
        return dto.getAll(page, size);
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
    public List<OrderItemData> getOrderItems(@PathVariable Long id) {
        return dto.getOrderItems(id);
    }

    @ApiOperation(value = "Get invoice data for an order")
    @GetMapping("/{id}/invoice-data")
    public InvoiceData getInvoiceData(@PathVariable Long id) {
        return dto.getInvoiceData(id);
    }

    @ApiOperation(value = "Generate invoice PDF")
    @PostMapping("/{id}/invoice")
    public ResponseEntity<Resource> generateInvoice(@PathVariable Long id) {
        try {
            return dto.generateInvoice(id, Properties.INVOICE_SERVICE_URL);
        } catch (ApiException e) {
            logger.error("API Exception while generating invoice: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while generating invoice: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @ApiOperation(value = "Get orders by date range with pagination")
    @GetMapping("/filter/date")
    public List<OrderData> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return dto.getOrdersByDateRange(startDate, endDate, page, size);
    }

    @ApiOperation(value = "Get orders by date range")
    @GetMapping("/date-range")
    public ResponseEntity<?> getOrdersByDateRange(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        try {
            List<OrderData> orders = dto.getOrdersByDateRange(startDate, endDate, page, size);
            return ResponseEntity.ok(orders);
        } catch (ApiException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 