package com.increff.dto;

import com.increff.dao.DailySalesDao;
import com.increff.entity.DailySalesEntity;
import com.increff.model.sales.DailySalesData;
import com.increff.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DailySalesDto {

    @Autowired
    private DailySalesDao dailySalesDao;

    /**
     * Get daily sales data for a specific date
     */
    public DailySalesData getByDate(LocalDate date) throws ApiException {
        Optional<DailySalesEntity> entityOpt = dailySalesDao.selectByDate(date);
        if (!entityOpt.isPresent()) {
            throw new ApiException("No sales data found for date: " + date);
        }
        
        return convertToData(entityOpt.get());
    }

    /**
     * Get daily sales data for a date range
     */
    public List<DailySalesData> getByDateRange(LocalDate startDate, LocalDate endDate) throws ApiException {
        validateDateRange(startDate, endDate);
        
        // Implement a method in DAO to get data by date range
        List<DailySalesEntity> entities = dailySalesDao.selectByDateRange(startDate, endDate);
        
        return entities.stream()
                .map(this::convertToData)
                .collect(Collectors.toList());
    }

    /**
     * Get the latest daily sales data
     */
    public DailySalesData getLatest() throws ApiException {
        Optional<DailySalesEntity> entityOpt = dailySalesDao.selectLatest();
        if (!entityOpt.isPresent()) {
            throw new ApiException("No sales data found");
        }
        
        return convertToData(entityOpt.get());
    }

    /**
     * Convert entity to data object
     */
    private DailySalesData convertToData(DailySalesEntity entity) {
        DailySalesData data = new DailySalesData();
        data.setDate(entity.getDate().atStartOfDay(ZonedDateTime.now().getZone()));
        data.setTotalOrders(entity.getTotalOrders());
        data.setTotalItems(entity.getTotalItems());
        data.setTotalRevenue(entity.getTotalRevenue());
        data.setInvoicedOrderCount(entity.getInvoicedOrderCount());
        data.setInvoicedItemCount(entity.getInvoicedItemCount());
        return data;
    }

    /**
     * Validate date range
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) throws ApiException {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }
        
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        
        LocalDate now = LocalDate.now();
        if (startDate.isAfter(now) || endDate.isAfter(now)) {
            throw new ApiException("Dates cannot be in the future");
        }
        
        // Limit date range to 90 days to prevent performance issues
        if (startDate.plusDays(90).isBefore(endDate)) {
            throw new ApiException("Date range cannot exceed 90 days");
        }
    }
} 