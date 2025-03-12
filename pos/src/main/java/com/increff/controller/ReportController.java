package com.increff.controller;

import com.increff.dto.ReportDto;
import com.increff.model.SalesReportData;
import com.increff.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportDto dto;

    @ApiOperation(value = "Get Sales Report by Date Range")
    @GetMapping("/sales")
    public List<SalesReportData> getSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate) throws ApiException {
        try {
            // Parse the dates manually
            ZonedDateTime start = ZonedDateTime.parse(startDate);
            ZonedDateTime end = ZonedDateTime.parse(endDate);
            return dto.getSalesReport(start, end);
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Please use ISO-8601 format (e.g., 2023-01-01T00:00:00Z)");
        }
    }
} 