package com.increff.model.inventory;

import java.util.ArrayList;
import java.util.List;

import com.increff.model.UploadError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadResponse {
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<UploadError> errors = new ArrayList<>();
    private List<InventoryData> successfulEntries = new ArrayList<>();
} 