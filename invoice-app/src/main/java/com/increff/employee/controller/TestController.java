package com.increff.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.increff.employee.model.OrderData;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.*;

@RestController
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/api/test/orders")
    public List<OrderData> getAllOrders(@RequestHeader(value = "Cookie", required = false) String cookies) {
        String url = "http://localhost:9000/employee/api/orders";
        
        // Create headers and copy cookies if present
        HttpHeaders headers = new HttpHeaders();
        if (cookies != null) {
            headers.add("Cookie", cookies);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // Make request with headers
        ResponseEntity<OrderData[]> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            OrderData[].class
        );
        
        return Arrays.asList(response.getBody());
    }
} 