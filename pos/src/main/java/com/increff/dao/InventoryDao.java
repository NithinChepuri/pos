package com.increff.dao;

import com.increff.entity.InventoryEntity;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventorySearchForm;
import org.springframework.stereotype.Repository;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.StringBuilder;

@Repository
public class InventoryDao extends AbstractDao<InventoryEntity> {
    
    // Query constants
    private static final String SELECT_ALL = "SELECT i FROM InventoryEntity i";
    private static final String SELECT_BY_PRODUCT_ID = "SELECT i FROM InventoryEntity i WHERE i.productId = :productId";
    private static final String SELECT_BY_BARCODE = "SELECT i FROM InventoryEntity i JOIN ProductEntity p ON i.productId = p.id WHERE LOWER(p.barcode) LIKE LOWER(:barcode)";
    private static final String SELECT_BY_PRODUCT_NAME = "SELECT i FROM InventoryEntity i JOIN ProductEntity p ON i.productId = p.id WHERE LOWER(p.name) LIKE LOWER(:productName)";
    private static final String SELECT_BY_BARCODE_OR_PRODUCT_NAME = "SELECT i FROM InventoryEntity i JOIN ProductEntity p " +
            "ON i.productId = p.id WHERE LOWER(p.barcode) LIKE LOWER(:barcode) OR LOWER(p.name) LIKE LOWER(:productName)";
    
    // Search query components
    private static final String SEARCH_JOIN = " LEFT JOIN ProductEntity p ON i.productId = p.id";
    private static final String SEARCH_WHERE = " WHERE 1=1";
    private static final String SEARCH_BARCODE_CONDITION = "LOWER(p.barcode) LIKE LOWER(:barcode)";
    private static final String SEARCH_PRODUCT_NAME_CONDITION = "LOWER(p.name) LIKE LOWER(:productName)";
    private static final String SEARCH_AND_OPEN = " AND (";
    private static final String SEARCH_CLOSE = ")";

    public InventoryEntity insert(InventoryEntity inventory) {
        em.persist(inventory);
        return inventory;
    }

    public InventoryEntity select(Long id) {
        return super.select(InventoryEntity.class, id);
    }

    public List<InventoryEntity> selectAll() {
        return super.selectAll(InventoryEntity.class, SELECT_ALL);
    }

    public InventoryEntity selectByProductId(Long productId) {
        TypedQuery<InventoryEntity> query = getQuery(SELECT_BY_PRODUCT_ID, InventoryEntity.class);
        query.setParameter("productId", productId);
        List<InventoryEntity> inventories = query.getResultList();
        return inventories.isEmpty() ? null : inventories.get(0);
    }

    public InventoryEntity update(InventoryEntity inventory) {
        return em.merge(inventory);
    }

    public List<InventoryEntity> search(InventorySearchForm form, int page, int size) {
        StringBuilder query = new StringBuilder(SELECT_ALL);    
        query.append(SEARCH_JOIN);
        query.append(SEARCH_WHERE);
        
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();
        
        if (form.getBarcode() != null && !form.getBarcode().trim().isEmpty()) {
            conditions.add(SEARCH_BARCODE_CONDITION);
            params.put("barcode", "%" + form.getBarcode().trim() + "%");
        }
        
        if (form.getProductName() != null && !form.getProductName().trim().isEmpty()) {
            conditions.add(SEARCH_PRODUCT_NAME_CONDITION);
            params.put("productName", "%" + form.getProductName().trim() + "%");
        }
        
        if (!conditions.isEmpty()) {
            query.append(SEARCH_AND_OPEN);
            query.append(String.join(" OR ", conditions));
            query.append(SEARCH_CLOSE);
        }
        
        TypedQuery<InventoryEntity> jpaQuery = getQuery(query.toString(), InventoryEntity.class);
        params.forEach(jpaQuery::setParameter);
        
        // Apply pagination
        jpaQuery.setFirstResult(page * size);
        jpaQuery.setMaxResults(size);
        
        return jpaQuery.getResultList();
    }

    public List<InventoryEntity> searchByBarcode(String barcode) {
        return searchByBarcode(barcode, 0, 3);
    }

    public List<InventoryEntity> searchByBarcode(String barcode, int page, int size) {
        TypedQuery<InventoryEntity> query = getQuery(SELECT_BY_BARCODE, InventoryEntity.class);
        query.setParameter("barcode", "%" + barcode + "%");
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public List<InventoryEntity> searchByProductName(String productName) {
        return searchByProductName(productName, 0, 3);
    }

    public List<InventoryEntity> searchByProductName(String productName, int page, int size) {
        TypedQuery<InventoryEntity> query = getQuery(SELECT_BY_PRODUCT_NAME, InventoryEntity.class);
        query.setParameter("productName", "%" + productName + "%");
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public List<InventoryEntity> searchByBarcodeOrProductName(String barcode, String productName) {
        return searchByBarcodeOrProductName(barcode, productName, 0, 3);
    }

    public List<InventoryEntity> searchByBarcodeOrProductName(String barcode, String productName, int page, int size) {
        TypedQuery<InventoryEntity> query = getQuery(SELECT_BY_BARCODE_OR_PRODUCT_NAME, InventoryEntity.class);
        query.setParameter("barcode", "%" + barcode + "%");
        query.setParameter("productName", "%" + productName + "%");
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public List<InventoryEntity> selectAll(int page, int size) {
        TypedQuery<InventoryEntity> query = getQuery(SELECT_ALL, InventoryEntity.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
}