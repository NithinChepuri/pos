package com.increff.dto;

import com.increff.model.InventoryData;
import com.increff.model.InventoryForm;
import com.increff.entity.InventoryEntity;
import com.increff.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.increff.service.ApiException;
import com.increff.model.InventoryUploadForm;
import com.increff.service.ProductService;
import org.springframework.transaction.annotation.Transactional;
import com.increff.util.StringUtil;
import com.increff.entity.ProductEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryDto {

    @Autowired
    private InventoryService service;
    
    @Autowired
    private ProductService productService;

    public InventoryData add(InventoryForm form) throws ApiException {
        validateForm(form);
        InventoryEntity inventory = convert(form);
        return convert(service.add(inventory));
    }

    public InventoryData get(Long id) {
        return convert(service.get(id));
    }

    public List<InventoryData> getAll() {
        List<InventoryData> list = new ArrayList<>();
        for (InventoryEntity inventory : service.getAll()) {
            list.add(convert(inventory));
        }
        return list;
    }

    public void update(Long id, InventoryForm form) throws ApiException {
        validateForm(form);
        InventoryEntity inventory = service.get(id);
        inventory.setQuantity(form.getQuantity());
        service.update(inventory);
    }

    @Transactional
    public void bulkAdd(List<InventoryUploadForm> forms) throws ApiException {
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;
        
        for (InventoryUploadForm form : forms) {
            lineNumber++;
            try {
                validateForm(form, lineNumber);
                ProductEntity product = productService.getByBarcode(form.getBarcode());
                if (product == null) {
                    throw new ApiException("Product with barcode " + form.getBarcode() + " not found");
                }
                
                Integer quantity = Integer.parseInt(form.getQuantity());
                if (quantity < 0) {
                    throw new ApiException("Quantity cannot be negative");
                }
                
                InventoryEntity inventory = service.getByProductId(product.getId());
                if (inventory == null) {
                    inventory = new InventoryEntity();
                    inventory.setProductId(product.getId());
                    inventory.setQuantity(quantity);
                    service.add(inventory);
                } else {
                    inventory.setQuantity(inventory.getQuantity() + quantity);
                    service.update(inventory);
                }
            } catch (Exception e) {
                errors.add("Error at line " + lineNumber + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ApiException("Errors in TSV file:\n" + String.join("\n", errors));
        }
    }

    @Transactional(readOnly = true)
    public void validateForm(InventoryUploadForm form, int lineNumber) throws ApiException {
        if (StringUtil.isEmpty(form.getBarcode())) {
            throw new ApiException("Barcode cannot be empty");
        }
        try {
            Integer quantity = Integer.parseInt(form.getQuantity());
            if (quantity < 0) {
                throw new ApiException("Quantity cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid quantity format");
        }
    }

    private void validateForm(InventoryForm form) throws ApiException {
        if (form == null) {
            throw new ApiException("Form cannot be empty");
        }
        if (form.getProductId() == null) {
            throw new ApiException("Product ID cannot be empty");
        }
        if (form.getQuantity() == null || form.getQuantity() < 0) {
            throw new ApiException("Quantity must be non-negative");
        }
    }

    private InventoryEntity convert(InventoryForm form) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(form.getProductId());
        inventory.setQuantity(form.getQuantity());
        return inventory;
    }

    private InventoryData convert(InventoryEntity inventory) {
        InventoryData data = new InventoryData();
        data.setId(inventory.getId());
        data.setProductId(inventory.getProductId());
        data.setQuantity(inventory.getQuantity());
        return data;
    }

    public List<InventoryData> search(InventoryForm form) {
        List<InventoryEntity> inventories = service.search(form);
        List<InventoryData> list = new ArrayList<>();
        for (InventoryEntity inventory : inventories) {
            list.add(convert(inventory));
        }
        return list;
    }
} 