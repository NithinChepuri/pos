package com.increff.dto;

import com.increff.model.sales.DailySalesData;
import com.increff.service.ApiException;
import com.increff.service.DailySalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DailySalesDto {

    @Autowired
    private DailySalesService dailySalesService;

//    Get daily sales data for a specific date
    public DailySalesData getByDate(LocalDate date) throws ApiException {
        validateDate(date);
        return dailySalesService.getByDate(date);
    }

//    Get daily sales data for a date range
    public List<DailySalesData> getByDateRange(LocalDate startDate, LocalDate endDate) throws ApiException {
        validateDateRange(startDate, endDate);
        return dailySalesService.getByDateRange(startDate, endDate);
    }

//    Get the latest daily sales data
    public DailySalesData getLatest() throws ApiException {
        return dailySalesService.getLatest();
    }

//    validate date
    private void validateDate(LocalDate date) throws ApiException {
        if (date == null) {
            throw new ApiException("Date is required");
        }
        
        LocalDate now = LocalDate.now();
        if (date.isAfter(now)) {
            throw new ApiException("Date cannot be in the future");
        }
    }


//      Validate date range

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