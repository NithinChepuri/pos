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
        List<ProductForm> forms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                String[] fields = line.split("\t");
                if (fields.length >= REQUIRED_COLUMNS) {
                    forms.add(convertToProductForm(fields, lineNumber));
                }
            }
        }
        return forms;
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