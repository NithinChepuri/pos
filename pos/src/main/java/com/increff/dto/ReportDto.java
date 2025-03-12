package com.increff.dto;

import com.increff.model.SalesReportData;
import com.increff.service.ReportService;
import com.increff.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class ReportDto {

    @Autowired
    private ReportService service;

    public List<SalesReportData> getSalesReport(ZonedDateTime startDate, ZonedDateTime endDate) throws ApiException {
        validateDates(startDate, endDate);
        return service.getSalesReport(startDate, endDate);
    }

    private void validateDates(ZonedDateTime startDate, ZonedDateTime endDate) throws ApiException {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        ZonedDateTime now = ZonedDateTime.now();
        if (startDate.isAfter(now) || endDate.isAfter(now)) {
            throw new ApiException("Dates cannot be in the future");
        }
    }
} 