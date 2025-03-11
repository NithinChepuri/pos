package com.increff.controller;

import com.increff.dto.OrderDto;
import com.increff.model.OrderData;
import com.increff.model.OrderForm;
import com.increff.model.InvoiceData;
import com.increff.model.OrderItemData;
import com.increff.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderDto dto;

    @Autowired
    private RestTemplate restTemplate;

    private static final String INVOICE_SERVICE_URL = "http://localhost:8080/employee/api/invoice";

    @ApiOperation(value = "Create new order")
    @PostMapping
    public ResponseEntity<?> add(@RequestBody OrderForm form) {
        try {
            logger.info("Creating order with form: " + form);
            OrderData orderData = dto.add(form);
            return ResponseEntity.ok(orderData);
        } catch (ApiException e) {
            logger.error("API Exception while creating order: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while creating order: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
        }
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
    public List<OrderItemData> getOrderItems(@PathVariable Long id) throws ApiException {
        return dto.getOrderItems(id);
    }

    @ApiOperation(value = "Get invoice data for an order")
    @GetMapping("/{id}/invoice-data")
    public InvoiceData getInvoiceData(@PathVariable Long id) throws ApiException {
        return dto.getInvoiceData(id);
    }

    @ApiOperation(value = "Generate invoice PDF")
    @PostMapping("/{id}/invoice")
    public ResponseEntity<String> generateInvoice(@PathVariable Long id) throws ApiException {
        return dto.generateInvoice(id, INVOICE_SERVICE_URL);
    }
} 