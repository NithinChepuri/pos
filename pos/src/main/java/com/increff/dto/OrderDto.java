package com.increff.dto;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.model.orders.OrderItemData;
import com.increff.model.inventory.InvoiceData;
import com.increff.model.enums.OrderStatus;
import com.increff.service.ApiException;
import com.increff.flow.OrderFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;

import java.time.ZonedDateTime;
import java.util.List;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.HashMap;

@Component
public class OrderDto {
    
    @Autowired
    private OrderFlow flow;
    
    private static final Logger logger = LoggerFactory.getLogger(OrderDto.class);

    public OrderData add(OrderForm form) throws ApiException {
        // Validate form
        validateOrderForm(form);
        
        // Convert form to domain model or entity
        OrderEntity orderEntity = convertFormToEntity(form);
        Map<String, OrderItemEntity> orderItemsMap = convertFormItemsToEntities(form.getItems());
        
        // Delegate to flow for business logic
        return flow.createOrder(orderEntity, orderItemsMap);
    }
    
    public OrderData get(Long id) throws ApiException {
        return flow.getOrder(id);
    }
    
    public List<OrderData> getAll(int page, int size) {
        return flow.getAllOrders(page, size);
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



    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        return flow.getInvoiceData(orderId);
    }

    public ResponseEntity<Resource> generateInvoice(Long orderId, String invoiceServiceUrl) throws ApiException {
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

    private OrderEntity convertFormToEntity(OrderForm form) {
        OrderEntity entity = new OrderEntity();
        entity.setClientId(form.getClientId());
        entity.setStatus(OrderStatus.CREATED);
        entity.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        return entity;
    }

    private Map<String, OrderItemEntity> convertFormItemsToEntities(List<OrderItemForm> items) {
        Map<String, OrderItemEntity> barcodeToEntityMap = new HashMap<>();
        
        items.forEach(item -> {
            OrderItemEntity entity = new OrderItemEntity();
            entity.setQuantity(item.getQuantity());
            entity.setSellingPrice(item.getSellingPrice());
            barcodeToEntityMap.put(item.getBarcode(), entity);
        });
        
        return barcodeToEntityMap;
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