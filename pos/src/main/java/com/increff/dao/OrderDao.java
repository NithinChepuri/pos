package com.increff.dao;

import com.increff.entity.OrderEntity;
import org.springframework.stereotype.Repository;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Repository
public class OrderDao extends AbstractDao {
    
    private static final String SELECT_ALL = "SELECT o FROM OrderEntity o";
    private static final String SELECT_BY_DATE_RANGE = 
        "SELECT o FROM OrderEntity o WHERE o.createdAt BETWEEN :startDate AND :endDate";
    private static final String SELECT_BY_CLIENT_ID = "SELECT o FROM OrderEntity o " + "WHERE o.clientId = :clientId";
    public void insert(OrderEntity order) {
        em.persist(order);
    }
    
    public OrderEntity select(Long id) {
        return em.find(OrderEntity.class, id);
    }
    
    public List<OrderEntity> selectAll(int page, int size) {
        return selectAll(SELECT_ALL, OrderEntity.class, page, size);
    }

    public List<OrderEntity> selectByDateRange(ZonedDateTime startDate, ZonedDateTime endDate, int page, int size) {
        TypedQuery<OrderEntity> query = getQuery(SELECT_BY_DATE_RANGE, OrderEntity.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public List<OrderEntity> selectByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        ZonedDateTime startOfDay = startDate.atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime endOfDay = endDate.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC);

        TypedQuery<OrderEntity> query = getQuery(SELECT_BY_DATE_RANGE, OrderEntity.class);
        query.setParameter("startDate", startOfDay);
        query.setParameter("endDate", endOfDay);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public void update(OrderEntity order) {
        em.merge(order);
    }   
} 