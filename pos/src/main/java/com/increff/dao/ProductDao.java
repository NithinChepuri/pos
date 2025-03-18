package com.increff.dao;

import com.increff.entity.ProductEntity;
import com.increff.model.ProductForm;
import com.increff.model.ProductSearchForm;
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

    // Query constants
    private static final String SELECT_ALL = "SELECT p FROM ProductEntity p ORDER BY p.id";
    private static final String SELECT_BY_BARCODE = "SELECT p FROM ProductEntity p WHERE p.barcode=:barcode";
    private static final String BASE_SEARCH_QUERY = "SELECT DISTINCT p FROM ProductEntity p LEFT JOIN ClientEntity c ON p.clientId = c.id WHERE 1=1";
    private static final String NAME_CONDITION = "LOWER(p.name) LIKE LOWER(:name)";
    private static final String BARCODE_CONDITION = "LOWER(p.barcode) LIKE LOWER(:barcode)";
    private static final String CLIENT_ID_CONDITION = "p.clientId = :clientId";
    private static final String CLIENT_NAME_CONDITION = "LOWER(c.name) LIKE LOWER(:clientName)";

    public void insert(ProductEntity product) {
        em.persist(product);
    }

    public ProductEntity select(Long id) {
        return em.find(ProductEntity.class, id);
    }

    public List<ProductEntity> selectAll(int page, int size) {
        TypedQuery<ProductEntity> query = getQuery(SELECT_ALL, ProductEntity.class);
        applyPagination(query, page, size);
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

    public List<ProductEntity> search(ProductSearchForm form, int page, int size) {
        // Build the query with conditions
        QueryBuilder queryBuilder = buildSearchQuery(form);
        
        // Create and configure the JPA query
        TypedQuery<ProductEntity> jpaQuery = createJpaQuery(queryBuilder);
        
        // Apply pagination
        applyPagination(jpaQuery, page, size);
        
        // Execute and return results
        return jpaQuery.getResultList();
    }
    
    /**
     * Builds the search query based on the search form
     */
    private QueryBuilder buildSearchQuery(ProductSearchForm form) {
        QueryBuilder builder = new QueryBuilder();
        
        // Start with base query
        builder.setBaseQuery(BASE_SEARCH_QUERY);
        
        // Add name condition if provided
        addNameCondition(builder, form);
        
        // Add barcode condition if provided
        addBarcodeCondition(builder, form);
        
        // Add client ID condition if provided
        addClientIdCondition(builder, form);
        
        // Add client name condition if provided
        addClientNameCondition(builder, form);
        
        return builder;
    }
    
    /**
     * Adds product name search condition
     */
    private void addNameCondition(QueryBuilder builder, ProductSearchForm form) {
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            builder.addCondition(NAME_CONDITION);
            builder.addParameter("name", "%" + form.getName().trim() + "%");
        }
    }
    
    /**
     * Adds barcode search condition
     */
    private void addBarcodeCondition(QueryBuilder builder, ProductSearchForm form) {
        if (form.getBarcode() != null && !form.getBarcode().trim().isEmpty()) {
            builder.addCondition(BARCODE_CONDITION);
            builder.addParameter("barcode", "%" + form.getBarcode().trim() + "%");
        }
    }
    
    /**
     * Adds client ID search condition
     */
    private void addClientIdCondition(QueryBuilder builder, ProductSearchForm form) {
        if (form.getClientId() != null) {
            builder.addCondition(CLIENT_ID_CONDITION);
            builder.addParameter("clientId", form.getClientId());
        }
    }
    
    /**
     * Adds client name search condition
     */
    private void addClientNameCondition(QueryBuilder builder, ProductSearchForm form) {
        if (form.getClientName() != null && !form.getClientName().trim().isEmpty()) {
            builder.addCondition(CLIENT_NAME_CONDITION);
            builder.addParameter("clientName", "%" + form.getClientName().trim() + "%");
        }
    }
    
    /**
     * Creates a JPA TypedQuery from the QueryBuilder
     */
    private TypedQuery<ProductEntity> createJpaQuery(QueryBuilder builder) {
        // Build the final query string
        String queryString = builder.buildQueryString();
        
        // Create the JPA query
        TypedQuery<ProductEntity> jpaQuery = getQuery(queryString, ProductEntity.class);
        
        // Set all parameters
        for (Map.Entry<String, Object> entry : builder.getParameters().entrySet()) {
            jpaQuery.setParameter(entry.getKey(), entry.getValue());
        }
        
        return jpaQuery;
    }
    
    /**
     * Applies pagination to the query
     */
    private void applyPagination(TypedQuery<ProductEntity> query, int page, int size) {
        query.setFirstResult(page * size);
        query.setMaxResults(size);
    }
    
    /**
     * Helper class to build queries with conditions and parameters
     */
    private static class QueryBuilder {
        private String baseQuery;
        private final List<String> conditions = new ArrayList<>();
        private final Map<String, Object> parameters = new HashMap<>();
        
        public void setBaseQuery(String baseQuery) {
            this.baseQuery = baseQuery;
        }
        
        public void addCondition(String condition) {
            conditions.add(condition);
        }
        
        public void addParameter(String name, Object value) {
            parameters.put(name, value);
        }
        
        public Map<String, Object> getParameters() {
            return parameters;
        }
        
        public String buildQueryString() {
            StringBuilder query = new StringBuilder(baseQuery);
            
            if (!conditions.isEmpty()) {
                query.append(" AND (");
                query.append(String.join(" OR ", conditions));
                query.append(")");
            }
            
            return query.toString();
        }
    }
} 