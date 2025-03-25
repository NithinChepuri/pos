package com.increff.dao;

import com.increff.entity.OrderItemEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class OrderItemDao extends AbstractDao {
    
    private static final String SELECT_BY_ORDER = "SELECT p FROM OrderItemEntity p WHERE p.orderId = :orderId";
    private static final String COUNT_BY_PRODUCT_ID = "SELECT COUNT(oi) FROM OrderItemEntity oi WHERE oi.productId = :productId";

    public void insert(OrderItemEntity orderItem) {
        em.persist(orderItem);
    }

    public List<OrderItemEntity> selectByOrderId(Long orderId) {
        TypedQuery<OrderItemEntity> query = getQuery(SELECT_BY_ORDER, OrderItemEntity.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }

    /**
     * Count order items by product ID
     */
    public long countByProductId(Long productId) {
        TypedQuery<Long> query = getQuery(COUNT_BY_PRODUCT_ID, Long.class);
        query.setParameter("productId", productId);
        return query.getSingleResult();
    }
} 