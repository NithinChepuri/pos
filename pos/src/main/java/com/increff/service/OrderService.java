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
        // Validate all items first
        validateItems(items);
        
        // Create order
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        
        // You need to get clientId from somewhere - either from session or request
        // For now, let's assume it comes from the OrderForm
        // order.setClientId(form.getClientId());
        
        dao.insert(order);
        
        System.out.println("Created order with ID: " + order.getId());

        // Process each item
        for (OrderItemForm item : items) {
            ProductEntity product = productService.getByBarcode(item.getBarcode());
            System.out.println("Processing item with barcode: " + item.getBarcode());
            System.out.println("Selling price: " + item.getSellingPrice());
            
            // Check and update inventory
            if (!inventoryService.checkAndAllocateInventory(product.getId(), item.getQuantity())) {
                throw new ApiException("Insufficient inventory for product: " + item.getBarcode());
            }

            // Create order item
            OrderItemEntity orderItem = new OrderItemEntity();
            //ToDO move it to util folder
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSellingPrice(item.getSellingPrice());
            itemDao.insert(orderItem);
            
            System.out.println("Created order item with ID: " + orderItem.getId());
        }

        return order;
    }

    @Transactional
    public void generateInvoice(Long orderId) throws ApiException {
        OrderEntity order = get(orderId);
        if (order == null) {
            throw new ApiException("Order not found with id: " + orderId);
        }
        
        // Update order status
        order.setStatus(OrderStatus.INVOICED);
        dao.update(order);
    }

    private String generateAndSaveInvoice(OrderEntity order) {
        // TODO: Implement invoice generation using invoice-app
        // This will be implemented when we create the invoice module
        return null;
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

    private void validateItems(List<OrderItemForm> items) throws ApiException {
        if (items == null || items.isEmpty()) {
            throw new ApiException("Order must have at least one item");
        }

        for (OrderItemForm item : items) {
            if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
                throw new ApiException("Barcode cannot be empty");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ApiException("Quantity must be positive for barcode: " + item.getBarcode());
            }
            if (item.getSellingPrice() == null || item.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("Selling price must be positive for barcode: " + item.getBarcode());
            }

            // Verify product exists
            ProductEntity product = productService.getByBarcode(item.getBarcode());
            if (product == null) {
                throw new ApiException("Product not found with barcode: " + item.getBarcode());
            }

            // Verify inventory exists and is sufficient
            InventoryEntity inventory = inventoryService.getByProductId(product.getId());
            if (inventory == null || inventory.getQuantity() < item.getQuantity()) {
                throw new ApiException("Insufficient inventory for product: " + item.getBarcode());
            }
        }
    }

    @Transactional(readOnly = true)
    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        if (orderId == null) {
            throw new ApiException("Order ID cannot be null");
        }

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

        List<InvoiceItemData> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Populate items
        for (OrderItemEntity item : orderItems) {
            InvoiceItemData invoiceItem = new InvoiceItemData();
            
            // Get product details
            ProductEntity product = productService.get(item.getProductId());
            
            invoiceItem.setProductName(product.getName());
            invoiceItem.setQuantity(item.getQuantity());
            invoiceItem.setSellingPrice(item.getSellingPrice());
            invoiceItem.setTotalPrice(item.getSellingPrice().multiply(new BigDecimal(item.getQuantity())));
            
            items.add(invoiceItem);
            totalAmount = totalAmount.add(invoiceItem.getTotalPrice());
        }

        invoiceData.setItems(items);
        invoiceData.setTotalAmount(totalAmount);

        return invoiceData;
    }
} 