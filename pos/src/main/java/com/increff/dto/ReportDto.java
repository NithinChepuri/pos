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

    public List<SalesReportData> getSalesReport(ZonedDateTime startDate, ZonedDateTime endDate, Long clientId) throws ApiException {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setClientId(clientId);
        return getSalesReport(form);
    }

    //TODO: remove the Exceptions where not required
    public List<SalesReportData> getSalesReport(SalesReportForm form) throws ApiException {
        try {
            validateForm(form);
            return service.getSalesReport(form.getStartDate(), form.getEndDate(), form.getClientId());
        } catch (Exception e) {
            throw new ApiException("Error generating sales report: " + e.getMessage());
        }
    }

    private void validateForm(SalesReportForm form) throws ApiException {
        if (form.getStartDate() == null || form.getEndDate() == null) {
            throw new ApiException("Start date and end date are required");
        }
        if (form.getStartDate().isAfter(form.getEndDate())) {
            throw new ApiException("Start date cannot be after end date");
        }
        ZonedDateTime now = ZonedDateTime.now();
        if (form.getStartDate().isAfter(now) || form.getEndDate().isAfter(now)) {
            throw new ApiException("Dates cannot be in the future");
        }
    }
} 