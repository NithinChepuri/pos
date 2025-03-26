package com.increff.dao;

import com.increff.entity.ClientEntity;
import com.increff.model.clients.ClientSearchForm;
import org.springframework.stereotype.Repository;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.StringBuilder;

@Repository
public class ClientDao extends AbstractDao<ClientEntity> {
    // Define all queries as constants for better maintainability
    private static final String SELECT_ALL = "SELECT c FROM ClientEntity c ORDER BY c.name";
    private static final String SELECT_BY_EMAIL = "SELECT c FROM ClientEntity c WHERE c.email = :email";
    private static final String SELECT_BY_NAME = "SELECT c FROM ClientEntity c WHERE c.name = :name";
    private static final String SEARCH_BASE = "SELECT c FROM ClientEntity c";
    private static final String WHERE_NAME_LIKE = " WHERE LOWER(c.name) LIKE LOWER(:name)";
    private static final String OR_EMAIL_LIKE = " OR LOWER(c.email) LIKE LOWER(:email)";
    private static final String WHERE_EMAIL_LIKE = " WHERE LOWER(c.email) LIKE LOWER(:email)";
    private static final String ORDER_BY_NAME = " ORDER BY c.name";


    public void insert(ClientEntity client) {
        em.persist(client);
    }

    public ClientEntity select(Long id) {
        return super.select(ClientEntity.class, id);
    }

    public List<ClientEntity> selectAll() {
        return super.selectAll(ClientEntity.class, SELECT_ALL);
    }

    public ClientEntity selectByEmail(String email) {
        TypedQuery<ClientEntity> query = getQuery(SELECT_BY_EMAIL, ClientEntity.class);
        query.setParameter("email", email);
        return getSingleResultOrNull(query);
    }

    public ClientEntity update(ClientEntity client) {
        return em.merge(client);
    }

    public void delete(ClientEntity client) {
        em.remove(em.contains(client) ? client : em.merge(client));
    }

    public ClientEntity selectByName(String name) {
        TypedQuery<ClientEntity> query = getQuery(SELECT_BY_NAME, ClientEntity.class);
        query.setParameter("name", name);
        return getSingleResultOrNull(query);
    }

    public List<ClientEntity> search(ClientSearchForm form) {
        StringBuilder queryBuilder = new StringBuilder(SEARCH_BASE);

        boolean hasSearchCriteria = false;
        
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            queryBuilder.append(WHERE_NAME_LIKE);
            hasSearchCriteria = true;
        }
        
        if (form.getEmail() != null && !form.getEmail().trim().isEmpty()) {
            if (hasSearchCriteria) {
                queryBuilder.append(OR_EMAIL_LIKE);
            } else {
                queryBuilder.append(WHERE_EMAIL_LIKE);
                hasSearchCriteria = true;
            }
        }
        
        // Add ORDER BY clause at the end
        queryBuilder.append(ORDER_BY_NAME);
        
        TypedQuery<ClientEntity> query = em.createQuery(queryBuilder.toString(), ClientEntity.class);
        
        // Set parameters if search criteria are provided
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            query.setParameter("name", "%" + form.getName().trim() + "%");
        }
        
        if (form.getEmail() != null && !form.getEmail().trim().isEmpty()) {
            query.setParameter("email", "%" + form.getEmail().trim() + "%");
        }
        
        return query.getResultList();
    }
    
    /**
     * Helper method to check if a string has value
     */
    private boolean hasValue(String str) {
        return str != null && !str.trim().isEmpty();
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