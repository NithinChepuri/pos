package com.increff.dto;

import com.increff.entity.OrderEntity;
import com.increff.model.OrderData;
import com.increff.model.OrderForm;
import com.increff.model.OrderItemData;
import com.increff.model.InvoiceData;
import com.increff.service.ApiException;
import com.increff.flow.OrderFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class OrderDto {
    
    @Autowired
    private OrderFlow flow;
    
    private static final Logger logger = LoggerFactory.getLogger(OrderDto.class);

    public OrderData add(OrderForm form) throws ApiException {
        // Validate form
        validateOrderForm(form);
        
        // Delegate to flow for business logic
        return flow.createOrder(form);
    }
    
    public OrderData get(Long id) throws ApiException {
        return flow.getOrder(id);
    }
    
    public List<OrderData> getAll() {
        return flow.getAllOrders();
    }
    
    public List<OrderData> getByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return flow.getOrdersByDateRange(startDate, endDate);
    }
    
    public List<OrderItemData> getOrderItems(Long orderId) throws ApiException {
        return flow.getOrderItems(orderId);
    }
    
    public void generateInvoice(Long orderId) throws ApiException {
        flow.generateInvoice(orderId);
    }

    public void cancelOrder(Long orderId) throws ApiException {
        flow.cancelOrder(orderId);
    }

    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        return flow.getInvoiceData(orderId);
    }

    public ResponseEntity<String> generateInvoice(Long orderId, String invoiceServiceUrl) throws ApiException {
        return flow.generateAndDownloadInvoice(orderId, invoiceServiceUrl);
    }

    private void validateOrderForm(OrderForm form) throws ApiException {
        if (form == null || form.getItems() == null || form.getItems().isEmpty()) {
            throw new ApiException("Order must have at least one item");
        }

        form.getItems().forEach(item -> {
            if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
                throw new ApiException("Barcode cannot be empty");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ApiException("Quantity must be positive");
            }
            if (item.getSellingPrice() == null || item.getSellingPrice().doubleValue() <= 0) {
                throw new ApiException("Selling price must be positive");
            }
        });
    }

    public ResponseEntity<?> createOrder(OrderForm form) {
        try {
            OrderData orderData = add(form);
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
} 