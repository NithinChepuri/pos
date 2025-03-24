package com.increff.service;

import com.increff.dao.DailySalesDao;
import com.increff.entity.DailySalesEntity;
import com.increff.model.sales.DailySalesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailySalesService {

    @Autowired
    private DailySalesDao dailySalesDao;

    @Transactional(readOnly = true)
    public DailySalesData getByDate(LocalDate date) throws ApiException {
        Optional<DailySalesEntity> entityOpt = dailySalesDao.selectByDate(date);
        if (!entityOpt.isPresent()) {
            throw new ApiException("No sales data found for date: " + date);
        }
        
        return convertToData(entityOpt.get());
    }

    @Transactional(readOnly = true)
    public List<DailySalesData> getByDateRange(LocalDate startDate, LocalDate endDate) throws ApiException {
        List<DailySalesEntity> entities = dailySalesDao.selectByDateRange(startDate, endDate);
        
        return entities.stream()
                .map(this::convertToData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
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
} 