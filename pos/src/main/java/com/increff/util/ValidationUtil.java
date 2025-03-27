package com.increff.util;

import com.increff.entity.InventoryEntity;
import com.increff.model.inventory.InventoryUpdateForm;
import com.increff.service.ApiException;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

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
} 