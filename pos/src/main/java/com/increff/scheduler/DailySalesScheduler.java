package com.increff.scheduler;

import com.increff.service.DailySalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailySalesScheduler {

    @Autowired
    private DailySalesService dailySalesService;

    // Run at 9:10 AM every day
    @Scheduled(cron = "0 10 9 * * ?")
    public void scheduleDailySalesCalculation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        dailySalesService.calculateAndStoreDailySales(yesterday);
    }
} 