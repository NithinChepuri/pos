package com.increff.controller;

import com.increff.dto.ReportDto;
import com.increff.model.SalesReportData;
import com.increff.model.SalesReportForm;
import com.increff.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportDto dto;

    @ApiOperation(value = "Get Sales Report by Date Range and Client")
    @GetMapping("/sales")
    public List<SalesReportData> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(required = false) Long clientId) throws ApiException {
        return dto.getSalesReport(startDate, endDate, clientId);
    }
    
    @ApiOperation(value = "Get Sales Report using Form")
    @PostMapping("/sales")
    public List<SalesReportData> getSalesReportWithForm(@RequestBody SalesReportForm form) throws ApiException {
        return dto.getSalesReport(form);
    }
} 