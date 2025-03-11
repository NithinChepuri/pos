package com.increff.dao;

import com.increff.entity.ProductEntity;
import com.increff.model.ProductForm;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
    private static final String SELECT_BY_BARCODE = "select p from ProductEntity p where p.barcode=:barcode";

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
        TypedQuery<ProductEntity> query = getQuery(SELECT_BY_BARCODE, ProductEntity.class);
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
        query.append(" left join ClientEntity c on p.clientId = c.id");
        query.append(" where 1=1");
        
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();
        
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            conditions.add("lower(p.name) like lower(:name)");
            params.put("name", "%" + form.getName().trim() + "%");
        }
        
        if (form.getBarcode() != null && !form.getBarcode().trim().isEmpty()) {
            conditions.add("lower(p.barcode) like lower(:barcode)");
            params.put("barcode", "%" + form.getBarcode().trim() + "%");
        }
        
        if (form.getClientName() != null && !form.getClientName().trim().isEmpty()) {
            conditions.add("lower(c.name) like lower(:clientName)");
            params.put("clientName", "%" + form.getClientName().trim() + "%");
        }
        
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