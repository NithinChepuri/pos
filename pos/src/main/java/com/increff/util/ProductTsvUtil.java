package com.increff.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import com.increff.model.products.ProductForm;

public class ProductTsvUtil {

    private static final String TSV_DELIMITER = "\t";
    private static final int EXPECTED_COLUMN_COUNT = 4; // clientId, name, barcode, mrp

    public static List<ProductForm> readProductsFromTsv(MultipartFile file) throws IOException {
        List<ProductForm> products = new ArrayList<>();
        
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
                    products.add(convertToProductForm(fields, lineNumber));
                } catch (IOException e) {
                    // Log the error but continue processing other rows
                    System.err.println("Error processing line " + lineNumber + ": " + e.getMessage());
                    // We could collect errors here if needed
                }
            }
        }
        
        return products;
    }

    private static ProductForm convertToProductForm(String[] fields, int lineNumber) throws IOException {
        try {
            ProductForm form = new ProductForm();
            form.setClientId(Long.parseLong(fields[0].trim()));
            form.setName(fields[1].trim());
            form.setBarcode(fields[2].trim());
            form.setMrp(new BigDecimal(fields[3].trim()));
            return form;
        } catch (NumberFormatException e) {
            throw new IOException("Invalid number format at line " + lineNumber + ": " + e.getMessage());
        }
    }
} 