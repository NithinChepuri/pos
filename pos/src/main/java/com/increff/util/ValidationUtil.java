package com.increff.util;

import com.increff.entity.InventoryEntity;
import com.increff.model.SalesReportForm;
import com.increff.model.inventory.InventoryUpdateForm;
import com.increff.model.orders.OrderForm;
import com.increff.model.orders.OrderItemForm;
import com.increff.service.ApiException;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class ValidationUtil {

    // Validate single date
    public static void validateDate(LocalDate date) throws ApiException {
        if (date == null) {
            throw new ApiException("Date is required");
        }
        
        LocalDate now = LocalDate.now();
        if (date.isAfter(now)) {
            throw new ApiException("Date cannot be in the future");
        }
    }

    // Validate date range
    public static void validateDateRange(LocalDate startDate, LocalDate endDate) throws ApiException {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }
        
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        
        LocalDate now = LocalDate.now();
        if (startDate.isAfter(now) || endDate.isAfter(now)) {
            throw new ApiException("Dates cannot be in the future");
        }
        
        // Limit date range to 90 days to prevent performance issues
        if (startDate.plusDays(90).isBefore(endDate)) {
            throw new ApiException("Date range cannot exceed 90 days");
        }
    }

    // Inventory validations
    public static void validateInventoryUpdateForm(InventoryUpdateForm form) throws ApiException {
        if (form == null) {
            throw new ApiException("Form cannot be null");
        }
        if (form.getQuantity() < 0) {
            throw new ApiException("Quantity cannot be negative");
        }
    }

    public static void validateInventoryFile(MultipartFile file) throws ApiException {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".tsv")) {
            throw new ApiException("Only TSV files are supported");
        }
    }

    public static void validateInventoryEntity(InventoryEntity inventory, Long id) throws ApiException {
        if (inventory == null) {
            throw new ApiException("Inventory not found with id: " + id);
        }
    }
    public static void validateReportForm(SalesReportForm form) throws ApiException {
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

    public static void validateOrderForm(OrderForm form) throws ApiException {
        Set<String> barcodes = new HashSet<>();
        for (OrderItemForm item : form.getItems()) {
            validateOrderItem(item);

            if (!barcodes.add(item.getBarcode())) {
                throw new ApiException("Duplicate product with barcode: " + item.getBarcode() + ". Please combine quantities instead.");
            }
        }
    }
    public static  void validateOrderItem(OrderItemForm item) throws ApiException {
        if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new ApiException("Quantity must be positive");
        }
        if (item.getSellingPrice() == null || item.getSellingPrice().doubleValue() <= 0) {
            throw new ApiException("Selling price must be positive");
        }
    }

    public static void validateDateRange(ZonedDateTime startDate, ZonedDateTime endDate) throws ApiException {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ApiException("Start date cannot be after end date");
        }

        if (startDate != null && startDate.isAfter(now)) {
            throw new ApiException("Start date cannot be in the future");
        }

        if (endDate != null && endDate.isAfter(now)) {
            throw new ApiException("End date cannot be in the future");
        }

        if (startDate != null && endDate != null) {
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 90) {
                throw new ApiException("Date range cannot exceed 3 months (90 days)");
            }
        }
    }
} 