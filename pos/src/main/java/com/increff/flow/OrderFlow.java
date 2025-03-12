package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderStatus;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.OrderData;
import com.increff.model.OrderForm;
import com.increff.model.OrderItemForm;
import com.increff.model.OrderItemData;
import com.increff.model.InvoiceData;
import com.increff.model.InvoiceItemData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import com.increff.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = ApiException.class)
public class OrderFlow {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private RestTemplate restTemplate;

    public OrderData createOrder(OrderForm form) throws ApiException {
        // Create empty order first
        OrderEntity order = orderService.createOrder();
        
        try {
            // Process each item
            for (OrderItemForm item : form.getItems()) {
                // Get product details
                ProductEntity product = productService.getByBarcode(item.getBarcode());
                if (product == null) {
                    throw new ApiException("Product not found with barcode: " + item.getBarcode());
                }

                // Check and update inventory
                if (!inventoryService.checkInventory(product.getId(), item.getQuantity().longValue())) {
                    throw new ApiException("Insufficient inventory for product: " + item.getBarcode());
                }
                inventoryService.updateInventory(product.getId(), -item.getQuantity().longValue());

                // Create order item
                OrderItemEntity orderItem = new OrderItemEntity();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(product.getId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSellingPrice(item.getSellingPrice());
                orderService.addOrderItem(orderItem);
            }

            return convertToOrderData(order);
        } catch (ApiException e) {
            // If anything fails, the transaction will be rolled back
            throw e;
        }
    }

    public OrderData getOrder(Long id) throws ApiException {
        OrderEntity order = orderService.get(id);
        if (order == null) {
            throw new ApiException("Order not found with id: " + id);
        }
        return convertToOrderData(order);
    }
    
    public List<OrderData> getAllOrders() {
        List<OrderEntity> orders = orderService.getAll();
        return orders.stream()
                .map(this::convertToOrderData)
                .collect(Collectors.toList());
    }
    
    public List<OrderData> getOrdersByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<OrderEntity> orders = orderService.getByDateRange(startDate, endDate);
        return orders.stream()
                .map(this::convertToOrderData)
                .collect(Collectors.toList());
    }
    
    public List<OrderItemData> getOrderItems(Long orderId) throws ApiException {
        List<OrderItemEntity> items = orderService.getOrderItems(orderId);
        return items.stream()
                .map(this::convertToItemData)
                .collect(Collectors.toList());
    }

    public void generateInvoice(Long orderId) throws ApiException {
        OrderEntity order = orderService.get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }
        
        // Update order status
        orderService.updateStatus(orderId, OrderStatus.INVOICED);
    }

    public void cancelOrder(Long orderId) throws ApiException {
        // Get order and its items
        OrderEntity order = orderService.get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }

        List<OrderItemEntity> items = orderService.getOrderItems(orderId);

        // Restore inventory for each item
        for (OrderItemEntity item : items) {
            inventoryService.updateInventory(item.getProductId(), item.getQuantity().longValue());
        }

        // Update order status
        orderService.updateStatus(orderId, OrderStatus.CANCELLED);
    }
    
    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        OrderEntity order = orderService.get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }
        
        List<OrderItemEntity> items = orderService.getOrderItems(orderId);
        
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setOrderDate(order.getCreatedAt());
        
        List<InvoiceItemData> invoiceItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItemEntity item : items) {
            ProductEntity product = productService.get(item.getProductId());
            
            InvoiceItemData invoiceItem = new InvoiceItemData();
            invoiceItem.setName(product.getName());
            invoiceItem.setBarcode(product.getBarcode());
            invoiceItem.setQuantity(item.getQuantity());
            invoiceItem.setUnitPrice(item.getSellingPrice());
            
            BigDecimal itemTotal = item.getSellingPrice().multiply(new BigDecimal(item.getQuantity()));
            invoiceItem.setAmount(itemTotal);
            
            invoiceItems.add(invoiceItem);
            total = total.add(itemTotal);
        }
        
        invoiceData.setItems(invoiceItems);
        invoiceData.setTotal(total);
        
        return invoiceData;
    }
    
    public ResponseEntity<String> generateAndDownloadInvoice(Long orderId, String invoiceServiceUrl) throws ApiException {
        try {
            // Get invoice data
            InvoiceData invoiceData = getInvoiceData(orderId);
            
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
            generateInvoice(orderId);

            return ResponseEntity.ok("Invoice generated successfully");
        } catch (Exception e) {
            throw new ApiException("Error generating invoice: " + e.getMessage());
        }
    }
    
    private OrderData convertToOrderData(OrderEntity order) {
        OrderData data = new OrderData();
        data.setId(order.getId());
        data.setStatus(order.getStatus());
        data.setCreatedAt(order.getCreatedAt());
        data.setInvoicePath(order.getInvoicePath());
        data.setClientId(order.getClientId());
        
        // Get order items
        List<OrderItemEntity> items = orderService.getOrderItems(order.getId());
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
} 