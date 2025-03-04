package com.increff.dao;

import com.increff.entity.ClientEntity;
import com.increff.model.ClientForm;
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
    
    @PersistenceContext
    private EntityManager em;

    public void insert(ClientEntity client) {
        em.persist(client);
    }

    public ClientEntity select(Long id) {
        return em.find(ClientEntity.class, id);
    }

    public List<ClientEntity> selectAll() {
        TypedQuery<ClientEntity> query = getQuery("select c from ClientEntity c", ClientEntity.class);
        return query.getResultList();
    }

    public ClientEntity selectByEmail(String email) {
        TypedQuery<ClientEntity> query = getQuery(
            "select c from ClientEntity c where c.email=:email", ClientEntity.class);
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
        TypedQuery<ClientEntity> query = getQuery(
            "select c from ClientEntity c where lower(c.name)=:name", ClientEntity.class);
        query.setParameter("name", name.toLowerCase());
        List<ClientEntity> clients = query.getResultList();
        return clients.isEmpty() ? null : clients.get(0);
    }

    public List<ClientEntity> search(ClientForm form) {
        StringBuilder query = new StringBuilder("select c from ClientEntity c where 1=1");
        Map<String, Object> params = new HashMap<>();
        
        List<String> conditions = new ArrayList<>();
        
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            conditions.add("lower(c.name) like lower(:name)");
            params.put("name", "%" + form.getName().trim() + "%");
        }
        
        if (!conditions.isEmpty()) {
            query.append(" and (");
            query.append(String.join(" OR ", conditions));
            query.append(")");
        }
        
        TypedQuery<ClientEntity> jpaQuery = getQuery(query.toString(), ClientEntity.class);
        params.forEach(jpaQuery::setParameter);
        
        return jpaQuery.getResultList();
    }
} 