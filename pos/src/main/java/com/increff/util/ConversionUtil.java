package com.increff.util;

import com.increff.entity.ProductEntity;
import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.InventoryEntity;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductData;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventoryData;
import com.increff.model.enums.OrderStatus;

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
        entity.setMrp(form.getMrp());
        entity.setClientId(form.getClientId());
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
} 