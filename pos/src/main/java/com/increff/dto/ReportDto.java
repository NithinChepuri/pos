package com.increff.dto;

import com.increff.model.SalesReportData;
import com.increff.service.ReportService;
import com.increff.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReportDto {

    @Autowired
    private ReportService service;

    public List<SalesReportData> getSalesReport(LocalDate startDate, LocalDate endDate) throws ApiException {
        validateDates(startDate, endDate);
        return service.getSalesReport(startDate, endDate);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) throws ApiException {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        LocalDate today = LocalDate.now();
        if (startDate.isAfter(today) || endDate.isAfter(today)) {
            throw new ApiException("Dates cannot be in the future");
        }
    }
} 