package com.increff.dao;

import com.increff.entity.ClientEntity;
import com.increff.model.clients.ClientSearchForm;
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
public class ClientDao extends AbstractDao {
    // Define all queries as constants for better maintainability
    private static final String SELECT_ALL = "SELECT c FROM ClientEntity c ORDER BY c.name";
    private static final String SELECT_BY_EMAIL = "SELECT c FROM ClientEntity c WHERE c.email = :email";
    private static final String SELECT_BY_NAME = "SELECT c FROM ClientEntity c WHERE c.name = :name";
    private static final String SEARCH_BASE = "SELECT c FROM ClientEntity c";

    @PersistenceContext
    private EntityManager em;

    /**
     * Insert a new client
     */
    public void insert(ClientEntity client) {
        em.persist(client);
    }

    /**
     * Select client by ID
     */
    public ClientEntity select(Long id) {
        return em.find(ClientEntity.class, id);
    }

    /**
     * Select all clients ordered by name
     */
    public List<ClientEntity> selectAll() {
        TypedQuery<ClientEntity> query = getQuery(SELECT_ALL, ClientEntity.class);
        return query.getResultList();
    }

    /**
     * Select client by email
     */
    public ClientEntity selectByEmail(String email) {
        TypedQuery<ClientEntity> query = getQuery(SELECT_BY_EMAIL, ClientEntity.class);
        query.setParameter("email", email);
        return getSingleResultOrNull(query);
    }

    /**
     * Update client
     */
    public ClientEntity update(ClientEntity client) {
        em.flush();
        return em.merge(client);
    }

    /**
     * Delete client
     */
    public void delete(ClientEntity client) {
        em.flush();
        em.remove(em.contains(client) ? client : em.merge(client));
    }

    /**
     * Select client by name
     */
    public ClientEntity selectByName(String name) {
        TypedQuery<ClientEntity> query = getQuery(SELECT_BY_NAME, ClientEntity.class);
        query.setParameter("name", name);
        return getSingleResultOrNull(query);
    }

    /**
     * Search clients by name or email
     */
    public List<ClientEntity> search(ClientSearchForm form) {
        // Build query using QueryBuilder helper class
        QueryBuilder queryBuilder = new QueryBuilder(SEARCH_BASE);
        
        // Add search conditions
        if (hasValue(form.getName())) {
            queryBuilder.addOrCondition("LOWER(c.name) LIKE LOWER(:name)");
            queryBuilder.addParameter("name", "%" + form.getName().trim() + "%");
        }
        
        if (hasValue(form.getEmail())) {
            queryBuilder.addOrCondition("LOWER(c.email) LIKE LOWER(:email)");
            queryBuilder.addParameter("email", "%" + form.getEmail().trim() + "%");
        }
        
        // Add order by clause
        queryBuilder.addOrderBy("c.name");
        
        // Create and execute query
        TypedQuery<ClientEntity> query = getQuery(queryBuilder.getQuery(), ClientEntity.class);
        queryBuilder.applyParameters(query);
        
        return query.getResultList();
    }
    
    /**
     * Helper method to check if a string has value
     */
    private boolean hasValue(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Helper method to get single result or null
     */
    private <T> T getSingleResultOrNull(TypedQuery<T> query) {
        List<T> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Helper class for building dynamic queries
     */
    private static class QueryBuilder {
        private final StringBuilder queryBuilder;
        private final Map<String, Object> parameters = new HashMap<>();
        private final List<String> conditions = new ArrayList<>();
        private boolean hasWhereClause = false;
        private boolean hasOrderBy = false;
        
        public QueryBuilder(String baseQuery) {
            this.queryBuilder = new StringBuilder(baseQuery);
        }
        
        public void addOrCondition(String condition) {
            conditions.add(condition);
        }
        
        public void addParameter(String name, Object value) {
            parameters.put(name, value);
        }
        
        public void addOrderBy(String orderBy) {
            if (!hasOrderBy) {
                queryBuilder.append(" ORDER BY ").append(orderBy);
                hasOrderBy = true;
            }
        }
        
        public String getQuery() {
            if (!conditions.isEmpty() && !hasWhereClause) {
                queryBuilder.insert(queryBuilder.length(), " WHERE (");
                queryBuilder.append(String.join(" OR ", conditions));
                queryBuilder.append(")");
                hasWhereClause = true;
            }
            
            return queryBuilder.toString();
        }
        
        public void applyParameters(TypedQuery<?> query) {
            parameters.forEach(query::setParameter);
        }
    }
} 