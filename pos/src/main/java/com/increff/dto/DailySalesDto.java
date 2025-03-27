package com.increff.dto;

import com.increff.entity.DailySalesEntity;
import com.increff.model.sales.DailySalesData;
import com.increff.service.ApiException;
import com.increff.service.DailySalesService;
import com.increff.util.ConversionUtil;
import com.increff.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DailySalesDto {

    @Autowired
    private DailySalesService dailySalesService;

//    Get daily sales data for a specific date
    public DailySalesData getByDate(LocalDate date) throws ApiException {
        ValidationUtil.validateDate(date);
        DailySalesEntity entity = dailySalesService.getByDate(date);
        return ConversionUtil.convertDailySalesEntityToData(entity);
    }

//    Get daily sales data for a date range
    public List<DailySalesData> getByDateRange(LocalDate startDate, LocalDate endDate) throws ApiException {
        ValidationUtil.validateDateRange(startDate, endDate);
        List<DailySalesEntity> entities = dailySalesService.getByDateRange(startDate, endDate);
        
        return entities.stream()
                .map(ConversionUtil::convertDailySalesEntityToData)
                .collect(Collectors.toList());
    }

//    Get the latest daily sales data
    public DailySalesData getLatest() throws ApiException {
        DailySalesEntity entity = dailySalesService.getLatest();
        return ConversionUtil.convertDailySalesEntityToData(entity);
    }

} 