package com.increff.dto;

import com.increff.model.SalesReportData;
import com.increff.model.SalesReportForm;
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

    public List<SalesReportData> getSalesReport(SalesReportForm form) throws ApiException {
        validateForm(form);
        return service.getSalesReport(form.getStartDate(), form.getEndDate(), form.getClientId());
    }

    private void validateForm(SalesReportForm form) throws ApiException {
        if (form.getStartDate() == null || form.getEndDate() == null) {
            throw new ApiException("Start date and end date are required");
        }
        if (form.getEndDate().isBefore(form.getStartDate())) {
            throw new ApiException("End date cannot be before start date");
        }
        ZonedDateTime now = ZonedDateTime.now();
        if (form.getStartDate().isAfter(now) || form.getEndDate().isAfter(now)) {
            throw new ApiException("Dates cannot be in the future");
        }
    }
} 