package com.increff.model.products;

import com.increff.model.UploadError;

import java.util.ArrayList;
import java.util.List;

public class UploadResult<T> {
    private int successCount;
    private int errorCount;
    private int totalRows;
    private List<T> successfulEntries = new ArrayList<>();
    private List<UploadError> errors = new ArrayList<>();

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public List<T> getSuccessfulEntries() {
        return successfulEntries;
    }

    public void setSuccessfulEntries(List<T> successfulEntries) {
        this.successfulEntries = successfulEntries;
    }

    public List<UploadError> getErrors() {
        return errors;
    }

    public void setErrors(List<UploadError> errors) {
        this.errors = errors;
    }

    public void addSuccess(T entry) {
        successfulEntries.add(entry);
        successCount++;
    }

    public void addError(int rowNumber, ProductForm form, String message) {
        String data = String.format("Barcode: %s, Name: %s, Client ID: %d", 
                                    form.getBarcode(), form.getName(), form.getClientId());
        errors.add(new UploadError(rowNumber, data, message));
        errorCount++;
    }
} 