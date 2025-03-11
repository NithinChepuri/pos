package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderStatus;
import com.increff.entity.OrderItemEntity;
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
import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InvoiceFlow {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        // Get order
        OrderEntity order = orderService.get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }

        // Get order items
        List<OrderItemEntity> orderItems = orderService.getOrderItems(orderId);

        // Create invoice data
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setOrderDate(order.getCreatedAt().toLocalDateTime());

        // Get items data and total
        List<InvoiceItemData> items = getInvoiceItemsData(orderItems);
        invoiceData.setItems(items);
        invoiceData.setTotalAmount(calculateTotalAmount(items));

        return invoiceData;
    }

    public void generateInvoice(Long orderId) throws ApiException {
        OrderEntity order = orderService.get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }
        orderService.updateStatus(orderId, OrderStatus.INVOICED);
    }

    private List<InvoiceItemData> getInvoiceItemsData(List<OrderItemEntity> orderItems) {
        List<InvoiceItemData> items = new ArrayList<>();
        
        for (OrderItemEntity item : orderItems) {
            InvoiceItemData invoiceItem = new InvoiceItemData();
            
            // Get product details
            ProductEntity product = productService.get(item.getProductId());
            
            invoiceItem.setProductName(product.getName());
            invoiceItem.setQuantity(item.getQuantity());
            invoiceItem.setSellingPrice(item.getSellingPrice());
            invoiceItem.setTotalPrice(item.getSellingPrice().multiply(new BigDecimal(item.getQuantity())));
            
            items.add(invoiceItem);
        }
        
        return items;
    }

    private BigDecimal calculateTotalAmount(List<InvoiceItemData> items) {
        return items.stream()
            .map(InvoiceItemData::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 