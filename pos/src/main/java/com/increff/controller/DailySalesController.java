package com.increff.controller;

import com.increff.dto.DailySalesDto;
import com.increff.model.sales.DailySalesData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/daily-sales")
public class DailySalesController {

    @Autowired
    private DailySalesDto dto;

    @ApiOperation(value = "Get daily sales data for a specific date")
    @GetMapping("/date/{date}")
    public DailySalesData getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dto.getByDate(date);
    }

    @ApiOperation(value = "Get daily sales data for a date range")
    @GetMapping("/range")
    public List<DailySalesData> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return dto.getByDateRange(startDate, endDate);
    }

    @ApiOperation(value = "Get the latest daily sales data")
    @GetMapping("/latest")
    public DailySalesData getLatest() {
        return dto.getLatest();
    }
} 