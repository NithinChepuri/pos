package com.increff.util;

import com.increff.entity.*;
import com.increff.model.clients.ClientData;
import com.increff.model.clients.ClientForm;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventoryData;
import com.increff.model.enums.OrderStatus;
import com.increff.model.products.ProductUpdateForm;
import com.increff.model.sales.DailySalesData;
import com.increff.model.inventory.InventoryUploadForm;
import com.increff.model.inventory.UploadResponse;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.increff.model.UploadError;
import com.increff.model.users.UserData;

public class ConversionUtil {

    // Product conversions
    public static ProductEntity convertProductFormToEntity(ProductForm form) {
        ProductEntity entity = new ProductEntity();
        entity.setName(form.getName());
        entity.setBarcode(form.getBarcode());
        entity.setClientId(form.getClientId());
        entity.setMrp(form.getMrp());
        return entity;
    }

    public static ProductData convertProductEntityToData(ProductEntity entity) {
        ProductData data = new ProductData();
        data.setId(entity.getId());
        data.setName(entity.getName());
        data.setBarcode(entity.getBarcode());
        data.setMrp(entity.getMrp());
        data.setClientId(entity.getClientId());
        return data;
    }

    public static List<ProductData> convertProductEntitiesToDataList(List<ProductEntity> entities) {
        return entities.stream()
                .map(ConversionUtil::convertProductEntityToData)
                .collect(Collectors.toList());
    }

    // Order conversions
    public static OrderEntity convertOrderFormToEntity(OrderForm form) {
        OrderEntity entity = new OrderEntity();
        entity.setStatus(OrderStatus.CREATED);
        entity.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        return entity;
    }

    public static Map<String, OrderItemEntity> convertOrderItemFormsToEntities(List<OrderItemForm> items) {
        Map<String, OrderItemEntity> barcodeToEntityMap = new HashMap<>();
        items.forEach(item -> {
            OrderItemEntity entity = new OrderItemEntity();
            entity.setQuantity(item.getQuantity());
            entity.setSellingPrice(item.getSellingPrice());
            barcodeToEntityMap.put(item.getBarcode(), entity);
        });
        return barcodeToEntityMap;
    }

    // Inventory conversions
    public static InventoryEntity convertInventoryFormToEntity(InventoryForm form) {
        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(form.getProductId());
        entity.setQuantity(form.getQuantity());
        return entity;
    }

    public static InventoryData convertInventoryEntityToData(InventoryEntity entity, String productName, String barcode) {
        InventoryData data = new InventoryData();
        data.setId(entity.getId());
        data.setProductId(entity.getProductId());
        data.setQuantity(entity.getQuantity());
        data.setProductName(productName);
        data.setBarcode(barcode);
        return data;
    }

    public static List<InventoryData> convertInventoryEntitiesToDataList(
            List<InventoryEntity> entities, 
            Map<Long, String> productNames, 
            Map<Long, String> barcodes) {
        return entities.stream()
                .map(entity -> convertInventoryEntityToData(
                    entity, 
                    productNames.get(entity.getProductId()),
                    barcodes.get(entity.getProductId())))
                .collect(Collectors.toList());
    }
    public static DailySalesData convertDailySalesEntityToData(DailySalesEntity entity) {
        DailySalesData data = new DailySalesData();
        data.setDate(entity.getDate().atStartOfDay(ZonedDateTime.now().getZone()));
        data.setTotalOrders(entity.getTotalOrders());
        data.setTotalItems(entity.getTotalItems());
        data.setTotalRevenue(entity.getTotalRevenue());
        data.setInvoicedOrderCount(entity.getInvoicedOrderCount());
        data.setInvoicedItemCount(entity.getInvoicedItemCount());
        return data;
    }

    public static ProductData convertProductEntityToProductData(ProductEntity product) {
        ProductData data = new ProductData();
        data.setId(product.getId());
        data.setName(product.getName());
        data.setBarcode(product.getBarcode());
        data.setMrp(product.getMrp());
        data.setClientId(product.getClientId());
        return data;
    }

    // Client conversions
    public static ClientEntity convertClientFormToEntity(ClientForm form) {
        if (form == null) return null;
        ClientEntity client = new ClientEntity();
        client.setName(form.getName());
        client.setEmail(form.getEmail());
        client.setPhoneNumber(form.getPhoneNumber());
        return client;
    }

    public static ClientData convertClientEntityToData(ClientEntity client) {
        if (client == null) return null;
        ClientData data = new ClientData();
        data.setId(client.getId());
        data.setName(client.getName());
        data.setEmail(client.getEmail());
        data.setPhoneNumber(client.getPhoneNumber());
        return data;
    }

    // Inventory upload error conversion
    public static UploadError convertToUploadError(int lineNumber, InventoryUploadForm form, String errorMessage) {
        return new UploadError(
            lineNumber,
            "Barcode: " + form.getBarcode() + ", Quantity: " + form.getQuantity(),
            errorMessage
        );
    }

    public static void initializeUploadResponse(UploadResponse response, int totalRows) {
        response.setTotalRows(totalRows);
        response.setSuccessCount(0);
        response.setErrorCount(0);
    }

    public static void updateUploadResponseResults(
            UploadResponse response, 
            List<UploadError> errors,
            List<InventoryData> successfulEntries) {
        response.setErrors(errors);
        response.setSuccessfulEntries(successfulEntries);
        response.setErrorCount(errors.size());
        response.setSuccessCount(successfulEntries.size());
    }

    // Additional Product conversions
    public static ProductEntity convertUpdateFormToEntity(ProductUpdateForm form) {
        ProductEntity entity = new ProductEntity();
        entity.setName(form.getName());
        entity.setBarcode(form.getBarcode());
        entity.setMrp(form.getMrp());
        entity.setClientId(form.getClientId());
        return entity;
    }



    // Note: This method already exists as convertProductEntityToData
    // We'll standardize on using that name instead of convertToProductData
    public static ProductData convertToProductData(ProductEntity product) {
        return convertProductEntityToData(product);
    }

    public static UserData convertUserEntityToData(UserEntity user) {
        UserData data = new UserData();
        data.setId(user.getId());
        data.setEmail(user.getEmail());
        data.setRole(user.getRole());
        return data;
    }

} 