package com.increff.service;

import com.increff.dao.OrderDao;
import com.increff.dao.OrderItemDao;
import com.increff.dao.ProductDao;
import com.increff.entity.*;
import com.increff.model.OrderItemForm;
import com.increff.model.InvoiceData;
import com.increff.model.InvoiceItemData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderDao dao;
    
    @Autowired
    private OrderItemDao itemDao;
    
    @Autowired
    private ProductDao productDao;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ClientService clientService;

    @Transactional
    public OrderEntity createOrder(List<OrderItemForm> items) throws ApiException {
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        dao.insert(order);

        // Process items
        processOrderItems(order, items);

        return order;
    }

    @Transactional
    public void generateInvoice(Long orderId) throws ApiException {
        OrderEntity order = get(orderId);
        order.setStatus(OrderStatus.INVOICED);
        dao.update(order);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> getByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return dao.selectByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<OrderItemEntity> getOrderItems(Long orderId) {
        return itemDao.selectByOrderId(orderId);
    }
    
    @Transactional(readOnly = true)
    public OrderEntity get(Long id) throws ApiException {
        OrderEntity order = dao.select(id);
        if (order == null) {
            throw new ApiException("Order with id " + id + " not found");
        }
        return order;
    }
    
    @Transactional(readOnly = true)
    public List<OrderEntity> getAll() {
        return dao.selectAll();
    }
    
    public void updateStatus(Long id, OrderStatus newStatus) throws ApiException {
        OrderEntity order = get(id);
        order.setStatus(newStatus);
    }

    @Transactional(readOnly = true)
    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        // Get order
        OrderEntity order = get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }

        // Get order items
        List<OrderItemEntity> orderItems = itemDao.selectByOrderId(orderId);

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

    private void processOrderItems(OrderEntity order, List<OrderItemForm> items) throws ApiException {
        for (OrderItemForm item : items) {
            ProductEntity product = productService.getByBarcode(item.getBarcode());
            
            // Update inventory
            inventoryService.updateInventory(product.getId(), -item.getQuantity());

            // Create order item
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSellingPrice(item.getSellingPrice());
            itemDao.insert(orderItem);
        }
    }
} 