package com.increff.controller;

import com.increff.dto.OrderDto;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.inventory.InvoiceData;
import com.increff.model.orders.OrderItemData;
import com.increff.model.Constants;
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

@Api
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderDto dto;

    @ApiOperation(value = "Create new order")
    @PostMapping
    public ResponseEntity<?> add(@RequestBody OrderForm form) {
        logger.info("Creating order with form: " + form);
        return dto.createOrder(form);
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
            return dto.generateInvoice(id, Constants.INVOICE_SERVICE_URL);
        } catch (ApiException e) {
            logger.error("API Exception while generating invoice: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while generating invoice: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
} 