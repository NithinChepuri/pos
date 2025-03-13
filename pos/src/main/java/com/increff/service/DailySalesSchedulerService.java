package com.increff.service;

import com.increff.dao.DailySalesDao;
import com.increff.entity.DailySalesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class DailySalesSchedulerService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DailySalesDao dailySalesDao;

    // Run at 10:18 AM every day
    @Scheduled(cron = "0 24 10 * * ?")
    @Transactional
    public void calculateDailySales() {
        try {
            // Get yesterday's date
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            // Check if we already have data for yesterday
            Optional<DailySalesEntity> existingData = dailySalesDao.selectByDate(yesterday);
            if (existingData.isPresent()) {
                // Data already exists for yesterday, no need to recalculate
                return;
            }
            
            // Calculate start and end time for yesterday
            ZonedDateTime startTime = yesterday.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime endTime = yesterday.plusDays(1).atStartOfDay(ZoneId.systemDefault());
            
            // Get total orders created yesterday
            String orderCountQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime";
            Query query = em.createQuery(orderCountQuery);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            Long orderCount = (Long) query.getSingleResult();
            Integer totalOrders = orderCount != null ? orderCount.intValue() : 0;
            
            // Get total items ordered yesterday - using a join with orderId
            String itemCountQuery = "SELECT SUM(oi.quantity) FROM OrderItemEntity oi, OrderEntity o WHERE oi.orderId = o.id AND o.createdAt >= :startTime AND o.createdAt < :endTime";
            query = em.createQuery(itemCountQuery);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            Long itemCount = (Long) query.getSingleResult();
            Integer totalItems = itemCount != null ? itemCount.intValue() : 0;
            
            // Get total revenue from yesterday
            String revenueQuery = "SELECT SUM(oi.sellingPrice * oi.quantity) FROM OrderItemEntity oi, OrderEntity o WHERE oi.orderId = o.id AND o.createdAt >= :startTime AND o.createdAt < :endTime";
            query = em.createQuery(revenueQuery);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            BigDecimal totalRevenue = (BigDecimal) query.getSingleResult();
            if (totalRevenue == null) {
                totalRevenue = BigDecimal.ZERO;
            }
            
            // Get invoiced orders count
            String invoicedOrdersQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime AND o.status = 'INVOICED'";
            query = em.createQuery(invoicedOrdersQuery);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            Long invoicedOrderCount = (Long) query.getSingleResult();
            Integer totalInvoicedOrders = invoicedOrderCount != null ? invoicedOrderCount.intValue() : 0;
            
            // Get invoiced items count
            String invoicedItemsQuery = "SELECT SUM(oi.quantity) FROM OrderItemEntity oi, OrderEntity o WHERE oi.orderId = o.id AND o.createdAt >= :startTime AND o.createdAt < :endTime AND o.status = 'INVOICED'";
            query = em.createQuery(invoicedItemsQuery);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            Long invoicedItemCount = (Long) query.getSingleResult();
            Integer totalInvoicedItems = invoicedItemCount != null ? invoicedItemCount.intValue() : 0;
            
            // Create and save the daily sales entity
            DailySalesEntity dailySales = new DailySalesEntity(
                yesterday,
                totalOrders,
                totalItems,
                totalRevenue,
                totalInvoicedOrders,
                totalInvoicedItems
            );
            
            dailySalesDao.insert(dailySales);
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
        }
    }
} 