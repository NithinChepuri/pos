package com.increff.service;

import com.increff.dao.InventoryDao;
import com.increff.dao.ProductDao;
import com.increff.entity.InventoryEntity;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventorySearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryDao dao;


    @Transactional
    public void add(InventoryEntity inventory) {
        if (inventory.getVersion() == null) {
            inventory.setVersion(0);
        }
        dao.insert(inventory);
    }

    @Transactional
    public void update(Long id, Long quantity) throws ApiException {
        InventoryEntity inventory = get(id);
        if (inventory == null) {
            throw new ApiException("Inventory not found with id: " + id);
        }
        inventory.setQuantity(quantity);
        dao.update(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryEntity get(Long id) {
        return dao.select(id);
    }

    @Transactional(readOnly = true)
    public List<InventoryEntity> getAll() {
        return dao.selectAll();
    }
    
    @Transactional(readOnly = true)
    public List<InventoryEntity> getAll(int page, int size) {
        return dao.selectAll(page, size);
    }

    @Transactional(readOnly = true)
    public InventoryEntity getByProductId(Long productId) {
        return dao.selectByProductId(productId);
    }

    @Transactional
    public InventoryEntity updateInventory(Long productId, Long quantity) throws ApiException {
        InventoryEntity inventory = dao.selectByProductId(productId);
        
        if (inventory == null) {
            // Create new inventory
            inventory = new InventoryEntity();
            inventory.setProductId(productId);
            inventory.setQuantity(quantity);
            inventory.setVersion(0); // Set version for new entity
            return dao.insert(inventory);
        } else {
            // Update existing inventory
            inventory.setQuantity(quantity);
            return dao.update(inventory);
        }
    }
    @Transactional
    public InventoryEntity decreaseInventory(Long productId, Long quantity) throws ApiException {
        InventoryEntity inventory = dao.selectByProductId(productId);
        inventory.setQuantity(inventory.getQuantity()+ quantity);
        return dao.update(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryEntity> search(InventorySearchForm form, int page, int size) {
        return dao.search(form, page, size);
    }

    @Transactional(readOnly = true)
    public List<InventoryEntity> search(InventorySearchForm form) {
        return search(form, 0, 3);
    }

    @Transactional(readOnly = true)
    public boolean checkInventory(Long productId, Long requiredQuantity) {
        InventoryEntity inventory = getByProductId(productId);
        return inventory != null && inventory.getQuantity() >= requiredQuantity;
    }


    @Transactional(readOnly = true)
    public boolean existsByProductId(Long productId) {
        return dao.selectByProductId(productId) != null;
    }


    @Transactional
    public void delete(Long id) throws ApiException {
        InventoryEntity inventory = get(id);
        if (inventory == null) {
            throw new ApiException("Inventory not found with id: " + id);
        }
        dao.delete(inventory);
    }
} 