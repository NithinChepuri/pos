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
    public void add(InventoryEntity inventory) {
        dao.insert(inventory);
    }

    @Transactional
    public void update(InventoryEntity inventory) {
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
    public InventoryEntity getByProductId(Long productId) {
        return dao.selectByProductId(productId);
    }

    @Transactional
    public void updateInventory(Long productId, Integer change) {
        InventoryEntity inventory = getByProductId(productId);
        inventory.setQuantity(inventory.getQuantity() + change);
        dao.update(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryEntity> search(InventoryForm form) {
        if (form.getBarcode() != null && form.getProductName() != null) {
            return dao.searchByBarcodeOrProductName(form.getBarcode(), form.getProductName());
        } else if (form.getBarcode() != null) {
            return dao.searchByBarcode(form.getBarcode());
        } else if (form.getProductName() != null) {
            return dao.searchByProductName(form.getProductName());
        }
        return dao.selectAll();
    }

    @Transactional(readOnly = true)
    public boolean checkInventory(Long productId, Integer requiredQuantity) {
        InventoryEntity inventory = getByProductId(productId);
        return inventory != null && inventory.getQuantity() >= requiredQuantity;
    }
} 