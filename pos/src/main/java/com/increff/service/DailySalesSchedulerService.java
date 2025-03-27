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

    // Run at 9:10 AM every day
    @Scheduled(cron = "0 10 9 * * ?")
    @Transactional
    public void calculateDailySales() {

            // Get yesterday's date
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            // Check if we already have data for yesterday
            if (isDataAlreadyExists(yesterday)) {
                return;
            }
            
            // Calculate sales data
            DailySalesEntity dailySales = calculateSalesData(yesterday);
            
            // Save the data
            dailySalesDao.insert(dailySales);

    }
    
    private boolean isDataAlreadyExists(LocalDate date) {
        Optional<DailySalesEntity> existingData = dailySalesDao.selectByDate(date);
        return existingData.isPresent();
    }
    
    private DailySalesEntity calculateSalesData(LocalDate date) {
        // Calculate start and end time for the date
        ZonedDateTime startTime = date.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        
        // Get total orders created on the date
        Integer totalOrders = getTotalOrders(startTime, endTime);
        
        // Get total items ordered on the date
        Integer totalItems = getTotalItems(startTime, endTime);
        
        // Get total revenue from the date
        BigDecimal totalRevenue = getTotalRevenue(startTime, endTime);
        
        // Get invoiced orders count
        Integer totalInvoicedOrders = getInvoicedOrdersCount(startTime, endTime);
        
        // Get invoiced items count
        Integer totalInvoicedItems = getInvoicedItemsCount(startTime, endTime);
        
        // Create and return the daily sales entity
        return new DailySalesEntity(
            date,
            totalOrders,
            totalItems,
            totalRevenue,
            totalInvoicedOrders,
            totalInvoicedItems
        );
    }
    
    private Integer getTotalOrders(ZonedDateTime startTime, ZonedDateTime endTime) {
        String orderCountQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime";
        Query query = em.createQuery(orderCountQuery);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Long orderCount = (Long) query.getSingleResult();
        return orderCount != null ? orderCount.intValue() : 0;
    }
    
    private Integer getTotalItems(ZonedDateTime startTime, ZonedDateTime endTime) {
        String itemCountQuery = "SELECT SUM(oi.quantity) FROM OrderItemEntity oi, OrderEntity o WHERE oi.orderId = o.id AND o.createdAt >= :startTime AND o.createdAt < :endTime";
        Query query = em.createQuery(itemCountQuery);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Long itemCount = (Long) query.getSingleResult();
        return itemCount != null ? itemCount.intValue() : 0;
    }
    
    private BigDecimal getTotalRevenue(ZonedDateTime startTime, ZonedDateTime endTime) {
        String revenueQuery = "SELECT SUM(oi.sellingPrice * oi.quantity) FROM OrderItemEntity oi, OrderEntity o WHERE oi.orderId = o.id AND o.createdAt >= :startTime AND o.createdAt < :endTime";
        Query query = em.createQuery(revenueQuery);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        BigDecimal totalRevenue = (BigDecimal) query.getSingleResult();
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
    
    private Integer getInvoicedOrdersCount(ZonedDateTime startTime, ZonedDateTime endTime) {
        String invoicedOrdersQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt < :endTime AND o.status = 'INVOICED'";
        Query query = em.createQuery(invoicedOrdersQuery);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Long invoicedOrderCount = (Long) query.getSingleResult();
        return invoicedOrderCount != null ? invoicedOrderCount.intValue() : 0;
    }
    
    private Integer getInvoicedItemsCount(ZonedDateTime startTime, ZonedDateTime endTime) {
        String invoicedItemsQuery = "SELECT SUM(oi.quantity) FROM OrderItemEntity oi, OrderEntity o WHERE oi.orderId = o.id AND o.createdAt >= :startTime AND o.createdAt < :endTime AND o.status = 'INVOICED'";
        Query query = em.createQuery(invoicedItemsQuery);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Long invoicedItemCount = (Long) query.getSingleResult();
        return invoicedItemCount != null ? invoicedItemCount.intValue() : 0;
    }
} 