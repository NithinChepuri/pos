package com.increff.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.increff.model.inventory.InventoryUploadForm;

public class InventoryTsvUtil {

    private static final String TSV_DELIMITER = "\t";
    private static final int EXPECTED_COLUMN_COUNT = 2; // barcode, quantity

    public static List<InventoryUploadForm> readInventoryFromTsv(MultipartFile file) throws IOException {
        List<InventoryUploadForm> inventoryList = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] fields = line.split(TSV_DELIMITER);
                
                // Validate field count
                if (fields.length < EXPECTED_COLUMN_COUNT) {
                    throw new IOException("Invalid TSV format at line " + lineNumber + ". Expected " + 
                                         EXPECTED_COLUMN_COUNT + " columns but found " + fields.length);
                }
                
                try {
                    InventoryUploadForm form = new InventoryUploadForm();
                    form.setBarcode(fields[0].trim());
                    form.setQuantity(fields[1].trim());
                    inventoryList.add(form);
                } catch (Exception e) {
                    // Log the error but continue processing other rows
                    System.err.println("Error processing line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        
        return inventoryList;
    }
} 