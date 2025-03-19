package com.increff.dao;

import com.increff.entity.ClientEntity;
import com.increff.model.ClientForm;
import com.increff.model.ClientSearchForm;
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
    private static final String SELECT_ALL = "select c from ClientEntity c";
    private static final String SELECT_BY_EMAIL = "select c from ClientEntity c where c.email=:email";
    private static final String SELECT_BY_NAME = "select c from ClientEntity c where c.name=:name";
    // private static final String SELECT_BY_ID = "select c from ClientEntity c where c.id=:id";

    @PersistenceContext
    private EntityManager em;

    public void insert(ClientEntity client) {
        em.persist(client);
    }

    public ClientEntity select(Long id) {
        return em.find(ClientEntity.class, id);
    }

    public List<ClientEntity> selectAll() {
        TypedQuery<ClientEntity> query = getQuery(SELECT_ALL, ClientEntity.class);
        return query.getResultList();
    }

    public ClientEntity selectByEmail(String email) {
        TypedQuery<ClientEntity> query = getQuery(SELECT_BY_EMAIL, ClientEntity.class);
        query.setParameter("email", email);
        List<ClientEntity> clients = query.getResultList();
        return clients.isEmpty() ? null : clients.get(0);
    }

    public ClientEntity update(ClientEntity client) {
        em.flush();
        return em.merge(client);
    }

    public void delete(ClientEntity client) {
        em.flush();
        em.remove(em.contains(client) ? client : em.merge(client));
    }

    public ClientEntity selectByName(String name) {
        TypedQuery<ClientEntity> query = getQuery(SELECT_BY_NAME, ClientEntity.class);
        query.setParameter("name", name);
        List<ClientEntity> clients = query.getResultList();
        return clients.isEmpty() ? null : clients.get(0);
    }

    public List<ClientEntity> search(ClientSearchForm form) {
        StringBuilder query = new StringBuilder(SELECT_ALL);
        Map<String, Object> params = new HashMap<>();
        
        List<String> conditions = new ArrayList<>();
        
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            conditions.add("lower(c.name) like lower(:name)");
            params.put("name", "%" + form.getName().trim() + "%");
        }
        
        if (form.getEmail() != null && !form.getEmail().trim().isEmpty()) {
            conditions.add("lower(c.email) like lower(:email)");
            params.put("email", "%" + form.getEmail().trim() + "%");
        }
        
        if (!conditions.isEmpty()) {
            query.append(" where (");
            query.append(String.join(" OR ", conditions));
            query.append(")");
        }
        
        TypedQuery<ClientEntity> jpaQuery = getQuery(query.toString(), ClientEntity.class);
        params.forEach(jpaQuery::setParameter);
        
        return jpaQuery.getResultList();
    }
} 