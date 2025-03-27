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
import java.util.List;
import java.util.Optional;

@Service
public class DailySalesService {

    @Autowired
    private DailySalesDao dailySalesDao;

    @Transactional
    public void calculateAndStoreDailySales(LocalDate date) {
        if (isDataAlreadyExists(date)) {
            return;
        }
        DailySalesEntity dailySales = calculateSalesData(date);
        dailySalesDao.insert(dailySales);
    }

    @Transactional(readOnly = true)
    public DailySalesEntity getByDate(LocalDate date) throws ApiException {
        Optional<DailySalesEntity> entityOpt = dailySalesDao.selectByDate(date);
        if (!entityOpt.isPresent()) {
            throw new ApiException("No sales data found for date: " + date);
        }
        
        return entityOpt.get();
    }

    @Transactional(readOnly = true)
    public List<DailySalesEntity> getByDateRange(LocalDate startDate, LocalDate endDate) throws ApiException {
        return dailySalesDao.selectByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public DailySalesEntity getLatest() throws ApiException {
        Optional<DailySalesEntity> entityOpt = dailySalesDao.selectLatest();
        if (!entityOpt.isPresent()) {
            throw new ApiException("No sales data found");
        }
        
        return entityOpt.get();
    }

    private boolean isDataAlreadyExists(LocalDate date) {
        Optional<DailySalesEntity> existingData = dailySalesDao.selectByDate(date);
        return existingData.isPresent();
    }
    
    private DailySalesEntity calculateSalesData(LocalDate date) {
        ZonedDateTime startTime = date.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        
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