package com.increff.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadError {
    private int rowNumber;
    private String data;
    private String message;

    // Add a no-args constructor
    public UploadError() {
        // Default constructor
    }

    public UploadError(int rowNumber, String data, String message) {
        this.rowNumber = rowNumber;
        this.data = data;
        this.message = message;
    }

    // Getters and setters
    public int getRowNumber() { return rowNumber; }
    public String getData() { return data; }
    public String getMessage() { return message; }
} 