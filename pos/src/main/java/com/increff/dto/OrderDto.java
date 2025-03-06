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
} 