package com.increff.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

@Repository
public abstract class AbstractDao {
    
    @PersistenceContext
    protected EntityManager em;
    
    protected <T> TypedQuery<T> getQuery(String jpql, Class<T> clazz) {
        return em.createQuery(jpql, clazz);
    }
    
    protected <T> TypedQuery<T> getQuery(String jpql, Class<T> clazz, int start, int length) {
        TypedQuery<T> query = em.createQuery(jpql, clazz);
        query.setFirstResult(start);
        query.setMaxResults(length);
        return query;
    }

    protected void flush() {
        em.flush();
    }

    protected void clear() {
        em.clear();
    }
} 