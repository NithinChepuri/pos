package com.increff.controller;

import com.increff.dto.ReportDto;
import com.increff.model.SalesReportData;
import com.increff.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws ApiException {
        return dto.getSalesReport(startDate, endDate);
    }
} 