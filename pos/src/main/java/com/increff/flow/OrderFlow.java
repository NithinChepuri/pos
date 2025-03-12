package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderStatus;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.OrderItemForm;
import com.increff.model.InvoiceData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import com.increff.service.InventoryService;
import com.increff.flow.InvoiceFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private InvoiceFlow invoiceFlow;

    public OrderEntity createOrder(List<OrderItemForm> items) throws ApiException {
        // Create empty order first
        OrderEntity order = orderService.createOrder();
        
        try {
            // Process each item
            for (OrderItemForm item : items) {
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

            return order;
        } catch (ApiException e) {
            // If anything fails, the transaction will be rolled back
            throw e;
        }
    }

    public void generateInvoice(Long orderId) throws ApiException {
        invoiceFlow.generateInvoice(orderId);
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

        // Update order status using the correct OrderStatus enum
        orderService.updateStatus(orderId, OrderStatus.CANCELLED);
    }
} 