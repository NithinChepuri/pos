package com.increff.dao;

import com.increff.entity.DailySalesEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DailySalesDao extends AbstractDao<DailySalesEntity> {

    private static final String SELECT_BY_DATE = "SELECT p FROM DailySalesEntity p WHERE p.date = :date";
    private static final String SELECT_BY_DATE_RANGE = "SELECT p FROM DailySalesEntity p WHERE p.date >= :startDate AND p.date <= :endDate ORDER BY p.date";
    private static final String SELECT_LATEST = "SELECT p FROM DailySalesEntity p ORDER BY p.date DESC";
    private static final String COUNT_ORDERS = "SELECT COUNT(o) FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime";
    private static final String COUNT_ITEMS = "SELECT SUM(oi.quantity) FROM OrderItemEntity oi JOIN oi.order o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime";
    private static final String SUM_REVENUE = "SELECT SUM(oi.sellingPrice * oi.quantity) FROM OrderItemEntity oi JOIN oi.order o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime";
    private static final String COUNT_INVOICED_ORDERS = "SELECT COUNT(o) FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime AND o.status = 'INVOICED'";
    private static final String COUNT_INVOICED_ITEMS = "SELECT SUM(oi.quantity) FROM OrderItemEntity oi JOIN oi.order o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime AND o.status = 'INVOICED'";

    @Transactional
    public void insert(DailySalesEntity dailySalesEntity) {
        em.persist(dailySalesEntity);
    }

    public Optional<DailySalesEntity> selectByDate(LocalDate date) {
        TypedQuery<DailySalesEntity> query = getQuery(SELECT_BY_DATE, DailySalesEntity.class);
        query.setParameter("date", date);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    public List<DailySalesEntity> selectByDateRange(LocalDate startDate, LocalDate endDate) {
        TypedQuery<DailySalesEntity> query = getQuery(SELECT_BY_DATE_RANGE, DailySalesEntity.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    public Optional<DailySalesEntity> selectLatest() {
        TypedQuery<DailySalesEntity> query = getQuery(SELECT_LATEST, DailySalesEntity.class);
        query.setMaxResults(1);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Integer getTotalOrders(ZonedDateTime startTime, ZonedDateTime endTime) {
        TypedQuery<Long> query = getQuery(COUNT_ORDERS, Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    public Integer getTotalItems(ZonedDateTime startTime, ZonedDateTime endTime) {
        TypedQuery<Long> query = getQuery(COUNT_ITEMS, Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    public BigDecimal getTotalRevenue(ZonedDateTime startTime, ZonedDateTime endTime) {
        TypedQuery<BigDecimal> query = getQuery(SUM_REVENUE, BigDecimal.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        BigDecimal total = query.getSingleResult();
        return total != null ? total : BigDecimal.ZERO;
    }

    public Integer getInvoicedOrdersCount(ZonedDateTime startTime, ZonedDateTime endTime) {
        TypedQuery<Long> query = getQuery(COUNT_INVOICED_ORDERS, Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    public Integer getInvoicedItemsCount(ZonedDateTime startTime, ZonedDateTime endTime) {
        TypedQuery<Long> query = getQuery(COUNT_INVOICED_ITEMS, Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }
} 