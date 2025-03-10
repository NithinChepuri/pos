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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Add name search condition
        if (form.getName() != null && !form.getName().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + form.getName().toLowerCase() + "%"));
        }
        
        // Add barcode search condition
        if (form.getBarcode() != null && !form.getBarcode().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("barcode")), "%" + form.getBarcode().toLowerCase() + "%"));
        }
        
        // Add client ID condition if needed
        if (form.getClientId() != null) {
            predicates.add(cb.equal(root.get("clientId"), form.getClientId()));
        }
        
        // Add all predicates to query
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        TypedQuery<ProductEntity> query = em.createQuery(cq);
        return query.getResultList();
    }
} 