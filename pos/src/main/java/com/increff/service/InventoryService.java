package com.increff.service;

import com.increff.dao.InventoryDao;
import com.increff.dao.ProductDao;
import com.increff.entity.InventoryEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.InventoryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import com.increff.service.ApiException;
import com.increff.model.InventoryUploadForm;
import com.increff.util.StringUtil;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryDao dao;

    @Autowired
    private ProductDao productDao;

    @Transactional
    public InventoryEntity add(InventoryEntity inventory) {
        // Check if product exists
        ProductEntity product = productDao.select(inventory.getProductId());
        if (product == null) {
            throw new ApiException("Product with ID " + inventory.getProductId() + " not found");
        }

        // Check if inventory already exists for this product
        InventoryEntity existing = dao.selectByProductId(inventory.getProductId());
        if (existing != null) {
            // Update existing inventory instead of creating new
            existing.setQuantity(existing.getQuantity() + inventory.getQuantity());
            return dao.update(existing);
        }

        if (inventory.getQuantity() < 0) {
            throw new ApiException("Quantity cannot be negative");
        }

        return dao.insert(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryEntity get(Long id) {
        InventoryEntity inventory = dao.select(id);
        if (inventory == null) {
            throw new ApiException("Inventory with ID " + id + " not found");
        }
        return inventory;
    }

    @Transactional(readOnly = true)
    public List<InventoryEntity> getAll() {
        return dao.selectAll();
    }

    @Transactional
    public void update(InventoryEntity inventory) {
        if (inventory.getQuantity() < 0) {
            throw new ApiException("Quantity cannot be negative");
        }
        dao.update(inventory);
    }

    @Transactional
    public void updateQuantity(Long id, Integer quantity) {
        InventoryEntity inventory = get(id);
        if (quantity < 0) {
            throw new ApiException("Quantity cannot be negative");
        }
        inventory.setQuantity(quantity);
        dao.update(inventory);
    }

    // Method to check and allocate inventory for orders
    public boolean checkAndAllocateInventory(Long productId, Integer requiredQuantity) {
        InventoryEntity inventory = dao.selectByProductId(productId);
        if (inventory == null || inventory.getQuantity() < requiredQuantity) {
            return false;
        }
        inventory.setQuantity(inventory.getQuantity() - requiredQuantity);
        dao.update(inventory);
        return true;
    }

    @Transactional(readOnly = true)
    public InventoryEntity getByProductId(Long productId) {
        return dao.selectByProductId(productId);
    }

    @Transactional
    public void updateInventory(Long productId, Integer change) throws ApiException {
        InventoryEntity inventory = getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        int newQuantity = inventory.getQuantity() + change;
        if (newQuantity < 0) {
            throw new ApiException("Cannot reduce inventory below 0");
        }
        
        inventory.setQuantity(newQuantity);
        dao.update(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryEntity> search(InventoryForm form) {
        return dao.search(form);
    }

    @Transactional
    public void bulkAdd(List<InventoryUploadForm> forms) throws ApiException {
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;
        
        for (InventoryUploadForm form : forms) {
            lineNumber++;
            try {
                validateForm(form, lineNumber);
                ProductEntity product = productDao.selectByBarcode(form.getBarcode());
                if (product == null) {
                    throw new ApiException("Product with barcode " + form.getBarcode() + " not found");
                }
                
                Integer quantity = Integer.parseInt(form.getQuantity());
                if (quantity < 0) {
                    throw new ApiException("Quantity cannot be negative");
                }
                
                InventoryEntity inventory = getByProductId(product.getId());
                if (inventory == null) {
                    inventory = new InventoryEntity();
                    inventory.setProductId(product.getId());
                    inventory.setQuantity(quantity);
                    add(inventory);
                } else {
                    inventory.setQuantity(inventory.getQuantity() + quantity);
                    update(inventory);
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
} 