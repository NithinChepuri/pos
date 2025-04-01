package com.increff.dao;

import com.increff.entity.ClientEntity;
import com.increff.model.clients.ClientSearchForm;
import org.springframework.stereotype.Repository;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class ClientDao extends AbstractDao<ClientEntity> {
    // Define all queries as constants for better maintainability
    private static final String SELECT_ALL = "SELECT c FROM ClientEntity c ORDER BY c.name";
    private static final String SELECT_BY_EMAIL = "SELECT c FROM ClientEntity c WHERE c.email = :email";
    private static final String SELECT_BY_NAME = "SELECT c FROM ClientEntity c WHERE c.name = :name";

    // Single query for searching clients by name and/or email
    private static final String SEARCH_CLIENTS = "SELECT c FROM ClientEntity c " +
            "WHERE (:nameProvided = 1 AND LOWER(c.name) LIKE LOWER(:nameParam)) " +
            "   OR (:emailProvided = 1 AND LOWER(c.email) LIKE LOWER(:emailParam)) " +
            "   OR (:nameProvided = 0 AND :emailProvided = 0) " + // Select all if neither provided
            "ORDER BY c.name";

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
        TypedQuery<ClientEntity> query = em.createQuery(SEARCH_CLIENTS, ClientEntity.class);

        boolean nameHasValue = hasValue(form.getName());
        boolean emailHasValue = hasValue(form.getEmail());

        query.setParameter("nameProvided", nameHasValue ? 1 : 0);
        query.setParameter("emailProvided", emailHasValue ? 1 : 0);
        query.setParameter("nameParam", nameHasValue ? "%" + form.getName().trim() + "%" : "%");
        query.setParameter("emailParam", emailHasValue ? "%" + form.getEmail().trim() + "%" : "%");

        return query.getResultList();
    }

    /**
     * Helper method to check if a string has value (not null or empty/whitespace).
     */
    private boolean hasValue(String str) {
        return str != null && !str.trim().isEmpty();
    }
} 