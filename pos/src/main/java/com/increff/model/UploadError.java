package com.increff.model;

public class UploadError {
    private int rowNumber;
    private String data;
    private String message;

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