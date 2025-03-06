package com.increff.controller;

import com.increff.dto.OrderDto;
import com.increff.model.OrderData;
import com.increff.model.OrderForm;
import com.increff.model.InvoiceData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
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
    private OrderService service;

    @Autowired
    private RestTemplate restTemplate;

    private static final String INVOICE_SERVICE_URL = "http://localhost:8080/employee/api/invoice";

    @ApiOperation(value = "Create new order")
    @PostMapping
    public ResponseEntity<?> add(@RequestBody OrderForm form) {
        try {
            // Log the incoming request
            logger.info("Creating order with form: " + form);
            
            // Validate form
            if (form == null || form.getItems() == null || form.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("Order must have items");
            }

            // Validate clientId
            if (form.getClientId() == null) {
                return ResponseEntity.badRequest().body("Client ID is required");
            }

            // Create order
            OrderData orderData = dto.add(form);
            return ResponseEntity.ok(orderData);
        } catch (ApiException e) {
            logger.error("API Exception while creating order: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
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
        try {
            // Get invoice data from our service
            InvoiceData invoiceData = service.getInvoiceData(id);
            
            // Generate base64 PDF
            ResponseEntity<String> base64Response = restTemplate.postForEntity(
                INVOICE_SERVICE_URL + "/generate",
                invoiceData,
                String.class
            );
            
            if (!base64Response.getStatusCode().is2xxSuccessful()) {
                throw new ApiException("Failed to generate invoice");
            }

            // Download PDF
            ResponseEntity<byte[]> downloadResponse = restTemplate.postForEntity(
                INVOICE_SERVICE_URL + "/download",
                invoiceData,
                byte[].class
            );
            
            if (!downloadResponse.getStatusCode().is2xxSuccessful()) {
                throw new ApiException("Failed to download invoice");
            }

            // Update order status to INVOICED after successful generation
            service.generateInvoice(id);

            return ResponseEntity.ok("Invoice generated successfully");
        } catch (Exception e) {
            throw new ApiException("Error generating invoice: " + e.getMessage());
        }
    }
} 