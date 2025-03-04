package com.increff.dao;

import com.increff.entity.ProductEntity;
import com.increff.model.ProductForm;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.StringBuilder;
import java.util.ArrayList;

@Repository
public class ProductDao extends AbstractDao {
    
    @PersistenceContext
    private EntityManager em;

    private static final String SELECT_ALL = "select p from ProductEntity p";

    public void insert(ProductEntity product) {
        em.persist(product);
    }

    public ProductEntity select(Long id) {
        return em.find(ProductEntity.class, id);
    }

    public List<ProductEntity> selectAll() {
        TypedQuery<ProductEntity> query = getQuery("select p from ProductEntity p", ProductEntity.class);
        return query.getResultList();
    }

    public ProductEntity selectByBarcode(String barcode) {
        TypedQuery<ProductEntity> query = getQuery(
            "select p from ProductEntity p where p.barcode=:barcode", ProductEntity.class);
        query.setParameter("barcode", barcode);
        List<ProductEntity> products = query.getResultList();
        return products.isEmpty() ? null : products.get(0);
    }

    public ProductEntity update(ProductEntity product) {
        ProductEntity merged = em.merge(product);
        flush();
        return merged;
    }

    public void delete(ProductEntity product) {
        em.remove(em.contains(product) ? product : em.merge(product));
        flush();
    }

    public List<ProductEntity> search(ProductForm form) {
        StringBuilder query = new StringBuilder("select distinct p from ProductEntity p");
        
        // Join with client if searching by client name
        if (form.getClientName() != null && !form.getClientName().trim().isEmpty()) {
            query.append(" left join ClientEntity c on p.clientId = c.id");
        }
        
        query.append(" where 1=1");
        Map<String, Object> params = new HashMap<>();
        
        // Build OR conditions for each non-null search criteria
        List<String> conditions = new ArrayList<>();
        
        if (form.getClientName() != null && !form.getClientName().trim().isEmpty()) {
            conditions.add("lower(c.name) like lower(:clientName)");
            params.put("clientName", "%" + form.getClientName().trim() + "%");
        }
        
        if (form.getBarcode() != null && !form.getBarcode().trim().isEmpty()) {
            conditions.add("lower(p.barcode) like lower(:barcode)");
            params.put("barcode", "%" + form.getBarcode().trim() + "%");
        }
        
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            conditions.add("lower(p.name) like lower(:name)");
            params.put("name", "%" + form.getName().trim() + "%");
        }
        
        // MRP range as a single condition
        if (form.getMinMrp() != null || form.getMaxMrp() != null) {
            StringBuilder mrpCondition = new StringBuilder("(");
            if (form.getMinMrp() != null) {
                mrpCondition.append("p.mrp >= :minMrp");
                params.put("minMrp", form.getMinMrp());
            }
            if (form.getMinMrp() != null && form.getMaxMrp() != null) {
                mrpCondition.append(" and ");
            }
            if (form.getMaxMrp() != null) {
                mrpCondition.append("p.mrp <= :maxMrp");
                params.put("maxMrp", form.getMaxMrp());
            }
            mrpCondition.append(")");
            conditions.add(mrpCondition.toString());
        }
        
        // Add conditions with OR
        if (!conditions.isEmpty()) {
            query.append(" and (");
            query.append(String.join(" OR ", conditions));
            query.append(")");
        }
        
        TypedQuery<ProductEntity> jpaQuery = getQuery(query.toString(), ProductEntity.class);
        params.forEach(jpaQuery::setParameter);
        
        return jpaQuery.getResultList();
    }
} 