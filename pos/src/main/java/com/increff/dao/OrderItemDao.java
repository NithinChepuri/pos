package com.increff.dao;

import com.increff.entity.OrderItemEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class OrderItemDao extends AbstractDao {
    
    private static final String SELECT_BY_ORDER = "select p from OrderItemEntity p where p.orderId=:orderId";

    @PersistenceContext
    private EntityManager em;

    public void insert(OrderItemEntity orderItem) {
        em.persist(orderItem);
    }

    public List<OrderItemEntity> selectByOrderId(Long orderId) {
        TypedQuery<OrderItemEntity> query = getQuery(SELECT_BY_ORDER, OrderItemEntity.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }
} 