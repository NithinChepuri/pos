package com.increff.dao;

import com.increff.entity.InventoryEntity;
import com.increff.model.InventoryForm;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.StringBuilder;

@Repository
public class InventoryDao extends AbstractDao {
    
    private static final String SELECT_ALL = "select i from InventoryEntity i";
    private static final String SELECT_BY_PRODUCT_ID = "select i from InventoryEntity i where i.productId=:productId";

    @PersistenceContext
    private EntityManager em;

    public InventoryEntity insert(InventoryEntity inventory) {
        em.persist(inventory);
        return inventory;
    }

    public InventoryEntity select(Long id) {
        return em.find(InventoryEntity.class, id);
    }

    public List<InventoryEntity> selectAll() {
        TypedQuery<InventoryEntity> query = getQuery(SELECT_ALL, InventoryEntity.class);
        return query.getResultList();
    }

    public InventoryEntity selectByProductId(Long productId) {
        TypedQuery<InventoryEntity> query = getQuery(
            "select i from InventoryEntity i where i.productId=:productId", 
            InventoryEntity.class);
        query.setParameter("productId", productId);
        List<InventoryEntity> inventories = query.getResultList();
        return inventories.isEmpty() ? null : inventories.get(0);
    }

    public InventoryEntity update(InventoryEntity inventory) {
        return em.merge(inventory);
    }

    public List<InventoryEntity> search(InventoryForm form) {
        StringBuilder query = new StringBuilder("select distinct i from InventoryEntity i");
        query.append(" left join ProductEntity p on i.productId = p.id");
        query.append(" where 1=1");
        
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();
        
        if (form.getBarcode() != null && !form.getBarcode().trim().isEmpty()) {
            conditions.add("lower(p.barcode) like lower(:barcode)");
            params.put("barcode", "%" + form.getBarcode().trim() + "%");
        }
        
        if (!conditions.isEmpty()) {
            query.append(" and (");
            query.append(String.join(" OR ", conditions));
            query.append(")");
        }
        
        TypedQuery<InventoryEntity> jpaQuery = getQuery(query.toString(), InventoryEntity.class);
        params.forEach(jpaQuery::setParameter);
        
        return jpaQuery.getResultList();
    }
} 