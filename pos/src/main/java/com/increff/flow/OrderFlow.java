package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.model.enums.OrderStatus;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderItemData;
import com.increff.model.invoice.InvoiceData;
import com.increff.model.invoice.InvoiceItemData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import com.increff.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

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

    /**
     * Creates a new order with the given items
     */
    public OrderData createOrder(OrderEntity orderEntity, Map<String, OrderItemEntity> orderItemsMap) throws ApiException {
        // Create order
        OrderEntity order = orderService.createOrder(orderEntity);
        
        try {
            processOrderItems(order, orderItemsMap);
            return convertToOrderData(order);
        } catch (ApiException e) {
            // If anything fails, the transaction will be rolled back
            throw e;
        }
    }

    /**
     * Process each order item - validate product, check inventory, and save
     */
    private void processOrderItems(OrderEntity order, Map<String, OrderItemEntity> orderItemsMap) throws ApiException {
        for (Map.Entry<String, OrderItemEntity> entry : orderItemsMap.entrySet()) {
            String barcode = entry.getKey();
            OrderItemEntity item = entry.getValue();
            
            // Get product and validate
            ProductEntity product = findAndValidateProduct(barcode);
            
            // Check and update inventory
            checkAndUpdateInventory(product, item.getQuantity());
            
            // Set order ID and product ID
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            
            // Add the order item
            orderService.addOrderItem(item);
        }
    }
    
    /**
     * Find product by barcode and validate it exists
     */
    private ProductEntity findAndValidateProduct(String barcode) throws ApiException {
        ProductEntity product = productService.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product not found with barcode: " + barcode);
        }
        return product;
    }
    
    /**
     * Check if inventory is sufficient and update it
     */
    private void checkAndUpdateInventory(ProductEntity product, Integer quantity) throws ApiException {
        if (!inventoryService.checkInventory(product.getId(), quantity.longValue())) {
            throw new ApiException("Insufficient inventory for product: " + product.getBarcode());
        }
        inventoryService.updateInventory(product.getId(), -quantity.longValue());
    }

    /**
     * Get order by ID
     */
    public OrderData getOrder(Long id) throws ApiException {
        OrderEntity order = findOrderById(id);
        return convertToOrderData(order);
    }
    
    /**
     * Find order by ID and validate it exists
     */
    //todo : move to flow layers and write seperate function for checking
    private OrderEntity findOrderById(Long id) throws ApiException {
        OrderEntity order = orderService.get(id);
        if (order == null) {
            throw new ApiException("Order not found with id: " + id);
        }
        return order;
    }
    
    /**
     * Get all orders with pagination
     */
    public List<OrderData> getAllOrders(int page, int size) {
        List<OrderEntity> orders = orderService.getAll(page, size);
        return orders.stream()
                .map(this::convertToOrderData)
                .collect(Collectors.toList());
    }
    
    /**
     * Get orders by date range
     */
    public List<OrderData> getOrdersByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        // Use default pagination (page 0, size 10)
        return getOrdersByDateRange(startDate, endDate, 0, 10);
    }
    
    /**
     * Get orders by date range with pagination
     */
    public List<OrderData> getOrdersByDateRange(ZonedDateTime startDate, ZonedDateTime endDate, int page, int size) {
        List<OrderEntity> orders = orderService.getByDateRange(startDate, endDate, page, size);
        return orders.stream()
                .map(this::convertToOrderData)
                .collect(Collectors.toList());
    }
    
    /**
     * Get items for a specific order
     */
    public List<OrderItemData> getOrderItems(Long orderId) throws ApiException {
        List<OrderItemEntity> items = orderService.getOrderItems(orderId);
        return items.stream()
                .map(this::convertToItemData)
                .collect(Collectors.toList());
    }

    /**
     * Generate invoice for an order
     */
    public void generateInvoice(Long orderId) throws ApiException {
        OrderEntity order = findOrderById(orderId);
        orderService.updateStatus(orderId, OrderStatus.INVOICED);
    }

    /**
     * Get invoice data for an order
     */
    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        OrderEntity order = findOrderById(orderId);
        
        // Create invoice data
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setOrderDate(order.getCreatedAt());
        
        // Get order items and convert to invoice items
        List<OrderItemEntity> orderItems = orderService.getOrderItems(orderId);
        List<InvoiceItemData> invoiceItems = convertToInvoiceItems(orderItems);
        
        // Set items and calculate total
        invoiceData.setItems(invoiceItems);
        invoiceData.setTotal(calculateTotal(invoiceItems));
        
        return invoiceData;
    }
    
    /**
     * Convert order items to invoice items
     */
    private List<InvoiceItemData> convertToInvoiceItems(List<OrderItemEntity> orderItems) {
        List<InvoiceItemData> invoiceItems = new ArrayList<>();
        
        for (OrderItemEntity orderItem : orderItems) {
            ProductEntity product = productService.get(orderItem.getProductId());
            
            InvoiceItemData invoiceItem = new InvoiceItemData();
            invoiceItem.setName(product.getName());
            invoiceItem.setBarcode(product.getBarcode());
            invoiceItem.setQuantity(orderItem.getQuantity());
            invoiceItem.setUnitPrice(orderItem.getSellingPrice());
            
            BigDecimal itemTotal = orderItem.getSellingPrice().multiply(new BigDecimal(orderItem.getQuantity()));
            invoiceItem.setAmount(itemTotal);
            
            invoiceItems.add(invoiceItem);
        }
        
        return invoiceItems;
    }
    
    /**
     * Calculate total amount from invoice items
     */
    private BigDecimal calculateTotal(List<InvoiceItemData> items) {
        return items.stream()
            .map(InvoiceItemData::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Generate and download invoice PDF
     */
    public ResponseEntity<Resource> generateAndDownloadInvoice(Long orderId, String invoiceServiceUrl) throws ApiException {
        // Generate invoice first
        generateInvoice(orderId);
        
        // Get invoice data
        InvoiceData invoiceData = getInvoiceData(orderId);
        
        try {
            // Call invoice service to generate PDF
            ResponseEntity<byte[]> downloadResponse = callInvoiceService(invoiceServiceUrl, invoiceData);
            
            // Prepare response with PDF content
            return createPdfResponse(downloadResponse.getBody(), orderId);
        } catch (Exception e) {
            throw new ApiException("Error generating invoice: " + e.getMessage());
        }
    }
    
    /**
     * Call the invoice service to generate PDF
     */
    private ResponseEntity<byte[]> callInvoiceService(String invoiceServiceUrl, InvoiceData invoiceData) {
        // Create HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create HTTP entity with headers and body
        HttpEntity<InvoiceData> requestEntity = new HttpEntity<>(invoiceData, headers);
        
        // Make the request
        return restTemplate.exchange(
            invoiceServiceUrl,
            HttpMethod.POST,
            requestEntity,
            byte[].class
        );
    }
    
    /**
     * Create PDF response with appropriate headers
     */
    private ResponseEntity<Resource> createPdfResponse(byte[] pdfContent, Long orderId) {
        ByteArrayResource resource = new ByteArrayResource(pdfContent);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf");
        headers.setContentLength(pdfContent.length);
        
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
    
    /**
     * Convert OrderEntity to OrderData
     */
    private OrderData convertToOrderData(OrderEntity order) {
        OrderData data = new OrderData();
        data.setId(order.getId());
        data.setStatus(order.getStatus());
        data.setCreatedAt(order.getCreatedAt());
        data.setInvoicePath(order.getInvoicePath());
        // data.setClientId(order.getClientId());
        
        // Get order items
        List<OrderItemEntity> items = orderService.getOrderItems(order.getId());
        data.setItems(items.stream()
                .map(this::convertToItemData)
                .collect(Collectors.toList()));
        
        return data;
    }

    /**
     * Convert OrderItemEntity to OrderItemData
     */
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