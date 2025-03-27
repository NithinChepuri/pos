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
import com.increff.model.sales.DailySalesData;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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

} 