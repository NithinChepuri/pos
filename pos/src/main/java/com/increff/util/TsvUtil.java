package com.increff.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import com.increff.model.products.ProductForm;

public class TsvUtil {
    private static final int REQUIRED_COLUMNS = 4;

    public static List<ProductForm> readProductsFromTsv(MultipartFile file) throws IOException {
        List<ProductForm> products = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip header line
                if (lineNumber == 1) {
                    continue;
                }
                
                String[] values = line.split("\t");
                
                if (values.length < 4) {
                    throw new IOException("Invalid format at line " + lineNumber + ". Expected 4 columns.");
                }
                
                ProductForm product = new ProductForm();
                product.setName(values[0].trim());
                product.setBarcode(values[1].trim());
                
                try {
                    product.setClientId(Long.parseLong(values[2].trim()));
                    product.setMrp(new BigDecimal(values[3].trim()));
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid number format at line " + lineNumber + ": " + e.getMessage());
                }
                
                products.add(product);
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