package com.increff.model;

import java.util.ArrayList;
import java.util.List;

public class UploadResult<T> {
    private List<T> successfulEntries = new ArrayList<>();
    private List<UploadError> errors = new ArrayList<>();
    private int totalRows;
    private int successCount;
    private int errorCount;

    public void addSuccess(T entry) {
        successfulEntries.add(entry);
        successCount++;
    }

    public void addError(int rowNumber, String data, String message) {
        errors.add(new UploadError(rowNumber, data, message));
        errorCount++;
    }

    // Getters
    public List<T> getSuccessfulEntries() { return successfulEntries; }
    public List<UploadError> getErrors() { return errors; }
    public int getTotalRows() { return totalRows; }
    public int getSuccessCount() { return successCount; }
    public int getErrorCount() { return errorCount; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
} 