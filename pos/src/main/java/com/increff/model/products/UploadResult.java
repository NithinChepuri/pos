package com.increff.model.products;

import com.increff.model.UploadError;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadResult<T> {
    private int successCount;
    private int errorCount;
    private int totalRows;
    private List<T> successfulEntries = new ArrayList<>();
    private List<UploadError> errors = new ArrayList<>();

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