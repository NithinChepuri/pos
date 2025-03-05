package com.increff.employee.controller;

import com.increff.employee.model.InvoiceDetails;
import com.increff.employee.model.OrderData;
import com.increff.employee.model.OrderItemData;
import com.increff.employee.service.PDFGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    @Autowired
    private PDFGeneratorService pdfService;
    
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<String> generateInvoice(@PathVariable Long orderId) throws Exception {
        String baseUrl = "http://localhost:9000/employee/api";
        
        try {
            // Fetch order details
            OrderData order = restTemplate.getForObject(
                baseUrl + "/orders/" + orderId,
                OrderData.class
            );
            
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found");
            }
            
            // Fetch order items
            OrderItemData[] items = restTemplate.getForObject(
                baseUrl + "/orders/" + orderId + "/items",
                OrderItemData[].class
            );
            
            if (items == null || items.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No items found for order");
            }
            
            // Create invoice details
            InvoiceDetails details = new InvoiceDetails();
            details.setOrder(order);
            details.setOrderItems(Arrays.asList(items));
            
            // Generate PDF and return Base64 encoded string
            String base64PDF = pdfService.generateInvoice(details);
            return ResponseEntity.ok(base64PDF);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error accessing order service: " + e.getMessage());
        }
    }
} 