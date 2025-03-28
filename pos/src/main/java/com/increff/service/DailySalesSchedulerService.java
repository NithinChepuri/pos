package com.increff.service;

import com.increff.dao.DailySalesDao;
import com.increff.entity.DailySalesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class DailySalesSchedulerService {

    @Autowired
    private DailySalesDao dailySalesDao;

    // Removed @Scheduled annotation
    @Transactional
    public void calculateDailySales(LocalDate date) {
        // Check if we already have data for the date
        if (isDataAlreadyExists(date)) {
            System.out.println("Data already exists for date: " + date + ". Skipping calculation.");
            return;
        }
        
        // Calculate sales data
        DailySalesEntity dailySales = calculateSalesData(date);
        
        // Save the data
        System.out.println("Inserting daily sales data for date: " + date);
        System.out.println("Data: " + dailySales.getTotalOrders() + " orders, " + 
                          dailySales.getTotalItems() + " items, " + 
                          dailySales.getTotalRevenue() + " revenue");
        
        try {
            dailySalesDao.insert(dailySales);
            System.out.println("Daily sales data inserted successfully");
        } catch (Exception e) {
            System.err.println("Error inserting daily sales data: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to ensure transaction rollback
        }
    }
    
    private boolean isDataAlreadyExists(LocalDate date) {
        Optional<DailySalesEntity> existingData = dailySalesDao.selectByDate(date);
        return existingData.isPresent();
    }
    
    private DailySalesEntity calculateSalesData(LocalDate date) {
        // Calculate start and end time for the date
        ZonedDateTime startTime = date.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        
        // Get metrics from DAO
        Integer totalOrders = dailySalesDao.getTotalOrders(startTime, endTime);
        Integer totalItems = dailySalesDao.getTotalItems(startTime, endTime);
        BigDecimal totalRevenue = dailySalesDao.getTotalRevenue(startTime, endTime);
        Integer totalInvoicedOrders = dailySalesDao.getInvoicedOrdersCount(startTime, endTime);
        Integer totalInvoicedItems = dailySalesDao.getInvoicedItemsCount(startTime, endTime);
        
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
} 