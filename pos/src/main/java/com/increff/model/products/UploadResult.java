package com.increff.model.products;

import com.increff.model.UploadError;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadResult<T> {
    private int successCount = 0;
    private int errorCount = 0;
    private int totalRows = 0;
    private List<T> successfulEntries = new ArrayList<>();
    private List<UploadError> errors = new ArrayList<>();

    public void addSuccess(T entry) {
        successfulEntries.add(entry);
        successCount++;
    }

    public void addError(int rowNumber, Object data, String message) {
        UploadError error = new UploadError();
        error.setRowNumber(rowNumber);
        error.setData(data != null ? data.toString() : "");
        error.setMessage(message);
        
        if (errors == null) {
            errors = new ArrayList<>();
        }
        
        errors.add(error);
        errorCount++;
    }

    @Getter
    @Setter
    public static class UploadError {
        private int rowNumber;
        private String data;
        private String message;
    }
} 