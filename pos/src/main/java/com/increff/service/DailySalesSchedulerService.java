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
        if (isDataAlreadyExists(date)) {
            return;
        }
        DailySalesEntity dailySales = calculateSalesData(date);
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
        
        // Get metrics from DAO
        Integer totalOrders = dailySalesDao.getTotalOrders(startTime, endTime);
        Integer totalItems = dailySalesDao.getTotalItems(startTime, endTime);
        BigDecimal totalRevenue = dailySalesDao.getTotalRevenue(startTime, endTime);
        Integer totalInvoicedOrders = dailySalesDao.getInvoicedOrdersCount(startTime, endTime);
        Integer totalInvoicedItems = dailySalesDao.getInvoicedItemsCount(startTime, endTime);
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