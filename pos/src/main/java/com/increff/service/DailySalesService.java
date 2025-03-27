package com.increff.service;

import com.increff.dao.DailySalesDao;
import com.increff.entity.DailySalesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DailySalesService {

    @Autowired
    private DailySalesDao dailySalesDao;

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
} 