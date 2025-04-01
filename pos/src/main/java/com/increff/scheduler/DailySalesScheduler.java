package com.increff.scheduler;

import com.increff.service.DailySalesSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailySalesScheduler {

    @Autowired
    private DailySalesSchedulerService dailySalesSchedulerService;

    // Run at specified time
    @Scheduled(cron = "0 59 11 * * ?")
    public void scheduleDailySalesCalculation() {
        // System.out.println("===== STARTING DAILY SALES CALCULATION =====");
        // System.out.println("Current time: " + java.time.LocalDateTime.now());
        
        
            LocalDate yesterday = LocalDate.now().minusDays(1);
            // System.out.println("Calculating sales for date: " + yesterday);
            
            dailySalesSchedulerService.calculateDailySales(yesterday);
            
            // System.out.println("Daily sales calculation completed successfully");
        
        
        // System.out.println("===== FINISHED DAILY SALES CALCULATION =====");
    }
} 