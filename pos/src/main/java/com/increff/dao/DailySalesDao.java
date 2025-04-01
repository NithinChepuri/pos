package com.increff.dao;

import com.increff.entity.DailySalesEntity;
import com.increff.service.ApiException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager em;

    private static final String SELECT_BY_DATE = "SELECT p FROM DailySalesEntity p WHERE p.date = :date";
    private static final String SELECT_BY_DATE_RANGE = "SELECT p FROM DailySalesEntity p WHERE p.date >= :startDate AND p.date <= :endDate ORDER BY p.date";
    private static final String SELECT_LATEST = "SELECT p FROM DailySalesEntity p ORDER BY p.date DESC";
    private static final String COUNT_ORDERS = "SELECT COUNT(o) FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime";
    private static final String COUNT_ITEMS = "SELECT SUM(oi.quantity) FROM OrderItemEntity oi WHERE oi.orderId IN (SELECT o.id FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime)";
    private static final String SUM_REVENUE = "SELECT SUM(oi.sellingPrice * oi.quantity) FROM OrderItemEntity oi WHERE oi.orderId IN (SELECT o.id FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime)";
    private static final String COUNT_INVOICED_ORDERS = "SELECT COUNT(o) FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime AND o.status = 'INVOICED'";
    private static final String COUNT_INVOICED_ITEMS = "SELECT SUM(oi.quantity) FROM OrderItemEntity oi WHERE oi.orderId IN (SELECT o.id FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime AND o.status = 'INVOICED')";

    @Transactional
    public void insert(DailySalesEntity dailySalesEntity) {
        try {
            // System.out.println("DAO: Inserting daily sales entity for date: " + dailySalesEntity.getDate());
            em.persist(dailySalesEntity);
            em.flush(); // Force immediate flush to detect any issues
            // System.out.println("DAO: Entity persisted successfully");
        } catch (ApiException e) {
            // System.err.println("DAO ERROR: Failed to insert daily sales entity: " + e.getMessage());
//            e.printStackTrace();
            throw e;
        }
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
        try {
            // System.out.println("Executing getTotalOrders query for time range: " + startTime + " to " + endTime);
            TypedQuery<Long> query = getQuery(COUNT_ORDERS, Long.class);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            Long count = query.getSingleResult();
            // System.out.println("Total orders count: " + count);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            // System.err.println("Error in getTotalOrders: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public Integer getTotalItems(ZonedDateTime startTime, ZonedDateTime endTime) {
        try {
            // System.out.println("Executing getTotalItems query for time range: " + startTime + " to " + endTime);
            TypedQuery<Long> query = getQuery(COUNT_ITEMS, Long.class);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            Long count = query.getSingleResult();
            // System.out.println("Total items count: " + count);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            // System.err.println("Error in getTotalItems: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public BigDecimal getTotalRevenue(ZonedDateTime startTime, ZonedDateTime endTime) {
        try {
            // System.out.println("Executing getTotalRevenue query for time range: " + startTime + " to " + endTime);
            TypedQuery<BigDecimal> query = getQuery(SUM_REVENUE, BigDecimal.class);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            BigDecimal total = query.getSingleResult();
            // System.out.println("Total revenue: " + total);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            // System.err.println("Error in getTotalRevenue: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public Integer getInvoicedOrdersCount(ZonedDateTime startTime, ZonedDateTime endTime) {
        try {
            // System.out.println("Executing getInvoicedOrdersCount query for time range: " + startTime + " to " + endTime);
            TypedQuery<Long> query = getQuery(COUNT_INVOICED_ORDERS, Long.class);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            Long count = query.getSingleResult();
            // System.out.println("Invoiced orders count: " + count);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            // System.err.println("Error in getInvoicedOrdersCount: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public Integer getInvoicedItemsCount(ZonedDateTime startTime, ZonedDateTime endTime) {
        try {
            // System.out.println("Executing getInvoicedItemsCount query for time range: " + startTime + " to " + endTime);
            TypedQuery<Long> query = getQuery(COUNT_INVOICED_ITEMS, Long.class);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            Long count = query.getSingleResult();
            // System.out.println("Invoiced items count: " + count);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            // System.err.println("Error in getInvoicedItemsCount: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
} 