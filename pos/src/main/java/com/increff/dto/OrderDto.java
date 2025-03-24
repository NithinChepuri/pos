package com.increff.dto;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.model.orders.OrderItemData;
import com.increff.model.invoice.InvoiceData;
import com.increff.model.enums.OrderStatus;
import com.increff.service.ApiException;
import com.increff.service.ProductService;
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
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

@Component
public class OrderDto {
    
    @Autowired
    private OrderFlow flow;
    
    @Autowired
    private ProductService productService;
    
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

    /**
     * Validate order form
     */
    private void validateOrderForm(OrderForm form) throws ApiException {
        if (form == null || form.getItems() == null || form.getItems().isEmpty()) {
            throw new ApiException("Order must have at least one item");
        }

        for (OrderItemForm item : form.getItems()) {
            if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
                throw new ApiException("Barcode cannot be empty");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ApiException("Quantity must be positive");
            }
            if (item.getSellingPrice() == null || item.getSellingPrice().doubleValue() <= 0) {
                throw new ApiException("Selling price must be positive");
            }
            
            // Validate selling price against MRP
            validateSellingPrice(item.getBarcode(), item.getSellingPrice());
        }
    }

    /**
     * Validate that selling price is not greater than MRP
     */
    private void validateSellingPrice(String barcode, BigDecimal sellingPrice) throws ApiException {
        // Get product by barcode
        ProductEntity product = productService.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product with barcode " + barcode + " not found");
        }
        
        // Compare selling price with MRP
        if (sellingPrice.compareTo(product.getMrp()) > 0) {
            throw new ApiException("Selling price (" + sellingPrice + ") cannot be greater than MRP (" + product.getMrp() + ") for product: " + product.getName());
        }
    }

    private OrderEntity convertFormToEntity(OrderForm form) {
        OrderEntity entity = new OrderEntity();
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
        }
    }

    /**
     * Get orders by date range with pagination
     */
    public List<OrderData> getOrdersByDateRange(ZonedDateTime startDate, ZonedDateTime endDate, int page, int size) throws ApiException {
        // Validate date range
        validateDateRange(startDate, endDate);
        
        return flow.getOrdersByDateRange(startDate, endDate, page, size);
    }

    /**
     * Validate date range for order filtering
     */
    private void validateDateRange(ZonedDateTime startDate, ZonedDateTime endDate) throws ApiException {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        
        // Check if start date is after end date
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ApiException("Start date cannot be after end date");
        }
        
        // Check if start date is in the future
        if (startDate != null && startDate.isAfter(now)) {
            throw new ApiException("Start date cannot be in the future");
        }
        
        // Check if end date is in the future
        if (endDate != null && endDate.isAfter(now)) {
            throw new ApiException("End date cannot be in the future");
        }
        
        // Check if date range exceeds 3 months (90 days)
        if (startDate != null && endDate != null) {
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 90) {
                throw new ApiException("Date range cannot exceed 3 months (90 days)");
            }
        }
    }
} 