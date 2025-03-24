package com.increff.controller;

import com.increff.dto.DailySalesDto;
import com.increff.model.sales.DailySalesData;
import com.increff.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/api/daily-sales")
public class DailySalesController {

    @Autowired
    private DailySalesDto dto;

    @ApiOperation(value = "Get daily sales data for a specific date")
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            DailySalesData data = dto.getByDate(date);
            return ResponseEntity.ok(data);
        } catch (ApiException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ApiOperation(value = "Get daily sales data for a date range")
    @GetMapping("/range")
    public ResponseEntity<?> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<DailySalesData> data = dto.getByDateRange(startDate, endDate);
            return ResponseEntity.ok(data);
        } catch (ApiException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ApiOperation(value = "Get the latest daily sales data")
    @GetMapping("/latest")
    public ResponseEntity<?> getLatest() {
        try {
            DailySalesData data = dto.getLatest();
            return ResponseEntity.ok(data);
        } catch (ApiException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 