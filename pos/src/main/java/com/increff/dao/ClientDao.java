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

    //todo make them as single query
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
        QueryBuilder queryBuilder = buildSearchQuery(form);
        TypedQuery<ClientEntity> query = em.createQuery(queryBuilder.getQuery(), ClientEntity.class);
        setSearchParameters(query, form);
        return query.getResultList();
    }

    private QueryBuilder buildSearchQuery(ClientSearchForm form) {
        QueryBuilder queryBuilder = new QueryBuilder(SEARCH_BASE);
        
        if (hasValue(form.getName())) {
            queryBuilder.addOrCondition(WHERE_NAME_LIKE);
        }
        
        if (hasValue(form.getEmail())) {
            if (queryBuilder.hasConditions()) {
                queryBuilder.addOrCondition(OR_EMAIL_LIKE);
            } else {
                queryBuilder.addOrCondition(WHERE_EMAIL_LIKE);
            }
        }
        
        queryBuilder.addOrderBy(ORDER_BY_NAME);
        return queryBuilder;
    }

    private void setSearchParameters(TypedQuery<ClientEntity> query, ClientSearchForm form) {
        if (hasValue(form.getName())) {
            query.setParameter("name", "%" + form.getName().trim() + "%");
        }
        
        if (hasValue(form.getEmail())) {
            query.setParameter("email", "%" + form.getEmail().trim() + "%");
        }
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
        private boolean hasConditions = false;
        
        public QueryBuilder(String baseQuery) {
            this.queryBuilder = new StringBuilder(baseQuery);
        }
        
        public void addOrCondition(String condition) {
            queryBuilder.append(condition);
            hasConditions = true;
        }

        public void addOrderBy(String orderBy) {
            queryBuilder.append(orderBy);
        }
        
        public String getQuery() {
            return queryBuilder.toString();
        }

        public boolean hasConditions() {
            return hasConditions;
        }
    }
} 