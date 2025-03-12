package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.OrderStatus;
import com.increff.entity.ProductEntity;
import com.increff.model.InvoiceData;
import com.increff.model.InvoiceItemData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InvoiceFlow {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    public void generateInvoice(Long orderId) throws ApiException {
        OrderEntity order = orderService.get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }
        
        // Update order status
        orderService.updateStatus(orderId, OrderStatus.INVOICED);
    }

    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        OrderEntity order = orderService.get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }
        
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        // Use ZonedDateTime directly
        invoiceData.setOrderDate(order.getCreatedAt());
        
        List<OrderItemEntity> orderItems = orderService.getOrderItems(orderId);
        List<InvoiceItemData> invoiceItems = new ArrayList<>();
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemEntity orderItem : orderItems) {
            ProductEntity product = productService.get(orderItem.getProductId());
            
            InvoiceItemData invoiceItem = new InvoiceItemData();
            // Use the new field names
            invoiceItem.setName(product.getName());
            invoiceItem.setBarcode(product.getBarcode());
            invoiceItem.setQuantity(orderItem.getQuantity());
            invoiceItem.setUnitPrice(orderItem.getSellingPrice());
            
            BigDecimal itemTotal = orderItem.getSellingPrice().multiply(new BigDecimal(orderItem.getQuantity()));
            invoiceItem.setAmount(itemTotal);
            
            invoiceItems.add(invoiceItem);
            totalAmount = totalAmount.add(itemTotal);
        }
        
        invoiceData.setItems(invoiceItems);
        // Use the new field name
        invoiceData.setTotal(totalAmount);
        
        return invoiceData;
    }
    
    // Calculate total amount from invoice items
    private BigDecimal calculateTotal(List<InvoiceItemData> items) {
        return items.stream()
            .map(InvoiceItemData::getAmount)  // Use the new method name
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 