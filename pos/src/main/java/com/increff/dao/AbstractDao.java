package com.increff.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public abstract class AbstractDao<T> {
    
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

    protected <T> T getSingle(TypedQuery<T> query) {
        List<T> list = query.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    protected <T> T getSingleResultOrNull(TypedQuery<T> query) {
        List<T> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public T select(Class<T> clazz, Long id) {
        return em.find(clazz, id);
    }

    public List<T> selectAll(Class<T> clazz, String queryStr) {
        TypedQuery<T> query = getQuery(queryStr, clazz);
        return query.getResultList();
    }

    public List<T> selectAll(String queryStr, Class<T> clazz, int page, int size) {
        TypedQuery<T> query = getQuery(queryStr, clazz);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
    
} 