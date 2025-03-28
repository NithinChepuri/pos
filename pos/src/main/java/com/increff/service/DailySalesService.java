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
        dailySalesSchedulerService.calculateDailySales(date);
    }

    @Transactional(readOnly = true)
    public DailySalesEntity getByDate(LocalDate date) throws ApiException {
        Optional<DailySalesEntity> optional = dailySalesDao.selectByDate(date);
        if (!optional.isPresent()) {
            throw new ApiException("No sales data found for date: " + date);
        }
        return optional.get();
    }

    @Transactional(readOnly = true)
    public List<DailySalesEntity> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return dailySalesDao.selectByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public DailySalesEntity getLatest() throws ApiException {
        Optional<DailySalesEntity> optional = dailySalesDao.selectLatest();
        if (!optional.isPresent()) {
            throw new ApiException("No sales data found");
        }
        return optional.get();
    }

    @Autowired
    private DailySalesSchedulerService dailySalesSchedulerService;
} 