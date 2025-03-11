package com.increff.dto;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.OrderData;
import com.increff.model.OrderForm;
import com.increff.model.OrderItemData;
import com.increff.model.OrderItemForm;
import com.increff.model.InvoiceData;
import com.increff.model.InvoiceItemData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import com.increff.flow.OrderFlow;
import com.increff.flow.InvoiceFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderDto {
    
    @Autowired
    private OrderFlow flow;
    
    @Autowired
    private OrderService service;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private InvoiceFlow invoiceFlow;

    @Autowired
    private RestTemplate restTemplate;

    public OrderData add(List<OrderItemForm> items) throws ApiException {
        // Validate items
        validateItems(items);
        
        // Create order through flow
        OrderEntity order = flow.createOrder(items);
        
        return convert(order);
    }

    public OrderData add(OrderForm form) throws ApiException {
        return add(form.getItems());
    }
    
    public OrderData get(Long id) throws ApiException {
        OrderEntity order = service.get(id);
        if (order == null) {
            throw new ApiException("Order not found with id: " + id);
        }
        return convert(order);
    }
    
    public List<OrderData> getAll() {
        return service.getAll().stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
    
    public List<OrderData> getByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return service.getByDateRange(startDate, endDate).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
    
    public List<OrderItemData> getOrderItems(Long orderId) throws ApiException {
        List<OrderItemEntity> items = service.getOrderItems(orderId);
        return items.stream()
                .map(this::convertToItemData)
                .collect(Collectors.toList());
    }
    
    public void generateInvoice(Long orderId) throws ApiException {
        flow.generateInvoice(orderId);
    }

    public void cancelOrder(Long orderId) throws ApiException {
        flow.cancelOrder(orderId);
    }

    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        return invoiceFlow.getInvoiceData(orderId);
    }

    public ResponseEntity<String> generateInvoice(Long orderId, String invoiceServiceUrl) throws ApiException {
        try {
            // Get invoice data
            InvoiceData invoiceData = invoiceFlow.getInvoiceData(orderId);
            
            // Generate base64 PDF
            ResponseEntity<String> base64Response = restTemplate.postForEntity(
                invoiceServiceUrl + "/generate",
                invoiceData,
                String.class
            );
            
            if (!base64Response.getStatusCode().is2xxSuccessful()) {
                throw new ApiException("Failed to generate invoice");
            }

            // Download PDF
            ResponseEntity<byte[]> downloadResponse = restTemplate.postForEntity(
                invoiceServiceUrl + "/download",
                invoiceData,
                byte[].class
            );
            
            if (!downloadResponse.getStatusCode().is2xxSuccessful()) {
                throw new ApiException("Failed to download invoice");
            }

            // Update order status
            invoiceFlow.generateInvoice(orderId);

            return ResponseEntity.ok("Invoice generated successfully");
        } catch (Exception e) {
            throw new ApiException("Error generating invoice: " + e.getMessage());
        }
    }

    private OrderData convert(OrderEntity order) {
        OrderData data = new OrderData();
        data.setId(order.getId());
        data.setStatus(order.getStatus());
        data.setCreatedAt(order.getCreatedAt());
        data.setInvoicePath(order.getInvoicePath());
        data.setClientId(order.getClientId());
        
        // Get order items
        List<OrderItemEntity> items = service.getOrderItems(order.getId());
        data.setItems(items.stream()
                .map(this::convertToItemData)
                .collect(Collectors.toList()));
        
        return data;
    }

    private OrderItemData convertToItemData(OrderItemEntity item) {
        OrderItemData itemData = new OrderItemData();
        ProductEntity product = productService.get(item.getProductId());
        
        itemData.setBarcode(product.getBarcode());
        itemData.setProductName(product.getName());
        itemData.setQuantity(item.getQuantity());
        itemData.setSellingPrice(item.getSellingPrice());
        itemData.setTotal(item.getSellingPrice().multiply(new BigDecimal(item.getQuantity())));
        
        return itemData;
    }

    private void validateItems(List<OrderItemForm> items) throws ApiException {
        if (items == null || items.isEmpty()) {
            throw new ApiException("Order must have at least one item");
        }

        for (OrderItemForm item : items) {
            if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
                throw new ApiException("Barcode cannot be empty");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ApiException("Quantity must be positive");
            }
            if (item.getSellingPrice() == null || item.getSellingPrice().doubleValue() <= 0) {
                throw new ApiException("Selling price must be positive");
            }
        }
    }
} 