package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.model.enums.OrderStatus;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.orders.OrderData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.model.orders.OrderItemData;
import com.increff.model.invoice.InvoiceData;
import com.increff.model.invoice.InvoiceItemData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import com.increff.service.InventoryService;
import com.increff.service.InvoiceCacheService;
import com.increff.service.client.InvoiceClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.time.ZoneOffset;

@Service
@Transactional(rollbackFor = ApiException.class)
public class OrderFlow {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InvoiceCacheService invoiceCacheService;

    @Autowired
    private InvoiceClientService invoiceClientService;


    public OrderData createOrder(OrderForm form) throws ApiException {
        validateSellingPrices(form);
        
        OrderEntity orderEntity = convertFormToEntity(form);
        Map<String, OrderItemEntity> orderItemsMap = convertFormItemsToEntities(form.getItems());
        
        // Create order
        OrderEntity order = orderService.createOrder(orderEntity);

        //todo convert in dto layer

            // Process order items
            processOrderItems(order, orderItemsMap);
            return convertToOrderData(order);

    }
    //todo move to dto
    public OrderData getOrder(Long id) throws ApiException {
        OrderEntity order = findOrderById(id);
        return convertToOrderData(order);
    }
    public List<OrderData> getAllOrders(int page, int size) {
        List<OrderEntity> orders = orderService.getAll(page, size);
        return orders.stream()
                .map(this::convertToOrderData)
                .collect(Collectors.toList());
    }
    //todo move this to service
    public void generateInvoice(Long orderId) throws ApiException {
        OrderEntity order = findOrderById(orderId);
        orderService.updateStatus(orderId, OrderStatus.INVOICED);
    }
    public InvoiceData getInvoiceData(Long orderId) throws ApiException {
        OrderEntity order = findOrderById(orderId);

        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setOrderDate(order.getCreatedAt());

        List<OrderItemEntity> orderItems = orderService.getOrderItems(orderId);
        List<InvoiceItemData> invoiceItems = convertToInvoiceItems(orderItems);
        invoiceData.setItems(invoiceItems);
        invoiceData.setTotal(calculateTotal(invoiceItems));

        return invoiceData;
    }
//    public List<OrderData> getOrdersByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
//        // Use default pagination (page 0, size 10)
//        return getOrdersByDateRange(startDate, endDate, 0, 10);
//    }
//    public List<OrderData> getOrdersByDateRange(ZonedDateTime startDate, ZonedDateTime endDate, int page, int size) {
//        List<OrderEntity> orders = orderService.getByDateRange(startDate, endDate, page, size);
//        return orders.stream()
//                .map(this::convertToOrderData)
//                .collect(Collectors.toList());
//    }

    public List<OrderData> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        // Use default pagination (page 0, size 10)
        return getOrdersByDateRange(startDate, endDate, 0, 10);
    }

    public List<OrderData> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        List<OrderEntity> orders = orderService.getByDateRange(startDate, endDate, page, size);
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


    public ResponseEntity<Resource> generateAndDownloadInvoice(Long orderId, String invoiceServiceUrl) throws ApiException {

        generateInvoice(orderId);

        InvoiceData invoiceData = getInvoiceData(orderId);

        try {
            ResponseEntity<byte[]> downloadResponse = invoiceClientService.generateInvoicePdf(invoiceServiceUrl, invoiceData);
            return createPdfResponse(downloadResponse.getBody(), orderId);
        } catch (Exception e) {
            throw new ApiException("Error generating or downloading invoice: " + e.getMessage());
        }
    }


    public ResponseEntity<Resource> generateAndCacheInvoice(Long orderId, String invoiceServiceUrl) throws ApiException {
        // Check if invoice is already in cache
        ResponseEntity<Resource> cachedResponse = invoiceCacheService.getFromCache(orderId);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // Generate new invoice
        ResponseEntity<Resource> response = generateAndDownloadInvoice(orderId, invoiceServiceUrl);

        // Cache the response for future requests
        return invoiceCacheService.cacheAndReturn(orderId, response);
    }

    private void validateSellingPrices(OrderForm form) throws ApiException {
        for (OrderItemForm item : form.getItems()) {
            ProductEntity product = productService.getByBarcode(item.getBarcode());
            if (product == null) {
                throw new ApiException("Product with barcode " + item.getBarcode() + " not found");
            }
            
            if (item.getSellingPrice().compareTo(product.getMrp()) > 0) {
                throw new ApiException("Selling price (" + item.getSellingPrice() + 
                    ") cannot be greater than MRP (" + product.getMrp() + 
                    ") for product: " + product.getName());
            }
        }
    }

    /**
     * Process each order item - validate product, check inventory, and save
     */
    private void processOrderItems(OrderEntity order, Map<String, OrderItemEntity> orderItemsMap) throws ApiException {
        for (Map.Entry<String, OrderItemEntity> entry : orderItemsMap.entrySet()) {
            String barcode = entry.getKey();
            OrderItemEntity item = entry.getValue();
            ProductEntity product = findAndValidateProduct(barcode);
            checkAndUpdateInventory(product, item.getQuantity());
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
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
        inventoryService.decreaseInventory(product.getId(), -quantity.longValue());
    }
    //todo move this to service
    private OrderEntity findOrderById(Long id) throws ApiException {
        OrderEntity order = orderService.get(id);
        if (order == null) {
            throw new ApiException("Order not found with id: " + id);
        }
        return order;
    }
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

    private BigDecimal calculateTotal(List<InvoiceItemData> items) {
        return items.stream()
            .map(InvoiceItemData::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ResponseEntity<Resource> createPdfResponse(byte[] pdfContent, Long orderId) {
        ByteArrayResource resource = new ByteArrayResource(pdfContent);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf");
        headers.setContentLength(pdfContent.length);
        
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private OrderData convertToOrderData(OrderEntity order) {
        OrderData data = new OrderData();
        data.setId(order.getId());
        data.setStatus(order.getStatus());
        data.setCreatedAt(order.getCreatedAt());
        data.setInvoicePath(order.getInvoicePath());
        
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
    private OrderEntity convertFormToEntity(OrderForm form) {
        OrderEntity entity = new OrderEntity();
        entity.setStatus(OrderStatus.CREATED);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setVersion(1);
        
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
} 