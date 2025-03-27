package com.increff.dto;

import com.increff.model.SalesReportData;
import com.increff.model.SalesReportForm;
import com.increff.service.ReportService;
import com.increff.service.ApiException;
import com.increff.util.ValidationUtil;
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

            ValidationUtil.validateReportForm(form);
            return service.getSalesReport(form.getStartDate(), form.getEndDate(), form.getClientId());

    }
} 