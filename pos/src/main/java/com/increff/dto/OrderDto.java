package com.increff.dto;

import com.increff.model.OrderData;
import com.increff.model.OrderForm;
import com.increff.model.OrderItemData;
import com.increff.model.OrderItemForm;
import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import com.increff.service.ApiException;
import com.increff.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.time.ZonedDateTime;
import java.math.BigDecimal;

@Component
public class OrderDto {
    
    @Autowired
    private OrderService service;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private InventoryService inventoryService;
    
    public OrderData add(OrderForm form) throws ApiException {
        OrderEntity order = service.createOrder(form.getItems());
        return convert(order);
    }
    
    public OrderData get(Long id) throws ApiException {
        return convert(service.get(id));
    }
    
    public List<OrderData> getAll() {
        List<OrderData> list = new ArrayList<>();
        for (OrderEntity order : service.getAll()) {
            list.add(convert(order));
        }
        return list;
    }
    
    public List<OrderData> getByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<OrderData> list = new ArrayList<>();
        for (OrderEntity order : service.getByDateRange(startDate, endDate)) {
            list.add(convert(order));
        }
        return list;
    }
    
    public List<OrderData> getOrderItems(Long orderId) throws ApiException {
        List<OrderData> list = new ArrayList<>();
        for (OrderItemEntity item : service.getOrderItems(orderId)) {
            list.add(convertItem(item));
        }
        return list;
    }
    
    private OrderData convert(OrderEntity order) {
        OrderData data = new OrderData();
        data.setId(order.getId());
        data.setCreatedAt(order.getCreatedAt());
        data.setStatus(order.getStatus());
        data.setInvoicePath(order.getInvoicePath());
        
        // Get order items
        List<OrderItemEntity> items = service.getOrderItems(order.getId());
        List<OrderItemData> itemDataList = new ArrayList<>();
        
        for (OrderItemEntity item : items) {
            OrderItemData itemData = new OrderItemData();
            ProductEntity product = productService.get(item.getProductId());
            
            itemData.setBarcode(product.getBarcode());
            itemData.setProductName(product.getName());
            itemData.setQuantity(item.getQuantity());
            itemData.setSellingPrice(item.getSellingPrice());
            itemData.setTotal(item.getSellingPrice().multiply(new BigDecimal(item.getQuantity())));
            
            itemDataList.add(itemData);
        }
        
        data.setItems(itemDataList);
        return data;
    }
    
    private OrderData convertItem(OrderItemEntity item) {
        OrderData data = new OrderData();
        ProductEntity product = productService.get(item.getProductId());
        
        List<OrderItemData> itemDataList = new ArrayList<>();
        OrderItemData itemData = new OrderItemData();
        
        itemData.setBarcode(product.getBarcode());
        itemData.setProductName(product.getName());
        itemData.setQuantity(item.getQuantity());
        itemData.setSellingPrice(item.getSellingPrice());
        itemData.setTotal(item.getSellingPrice().multiply(new BigDecimal(item.getQuantity())));
        
        itemDataList.add(itemData);
        data.setItems(itemDataList);
        return data;
    }

    public OrderEntity add(List<OrderItemForm> items) throws ApiException {
        // Validate form
        validateItems(items);
        // Call service
        return service.createOrder(items);
    }

    public void generateInvoice(Long orderId) throws ApiException {
        service.generateInvoice(orderId);
    }

    private void validateItems(List<OrderItemForm> items) throws ApiException {
        validateOrderNotEmpty(items);
        
        for (OrderItemForm item : items) {
            validateItemFields(item);
            validateProductExists(item.getBarcode());
            validateInventory(item);
        }
    }

    private void validateOrderNotEmpty(List<OrderItemForm> items) throws ApiException {
        if (items == null || items.isEmpty()) {
            throw new ApiException("Order must have at least one item");
        }
    }

    private void validateItemFields(OrderItemForm item) throws ApiException {
        // Validate barcode
        if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }
        
        // Validate quantity
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new ApiException("Quantity must be positive for barcode: " + item.getBarcode());
        }
        
        // Validate selling price
        if (item.getSellingPrice() == null || item.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Selling price must be positive for barcode: " + item.getBarcode());
        }
    }

    private void validateProductExists(String barcode) throws ApiException {
        ProductEntity product = productService.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product not found with barcode: " + barcode);
        }
    }

    private void validateInventory(OrderItemForm item) throws ApiException {
        ProductEntity product = productService.getByBarcode(item.getBarcode());
        if (product == null) {
            throw new ApiException("Product not found with barcode: " + item.getBarcode());
        }
        
        if (!inventoryService.checkInventory(product.getId(), item.getQuantity())) {
            throw new ApiException("Insufficient inventory for product: " + item.getBarcode());
        }
    }
} 