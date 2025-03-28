package com.increff.service;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class InvoiceCacheService {

    private static final String INVOICE_DIRECTORY = "cached_invoices/";

    public InvoiceCacheService() {
        // Create invoice directory if it doesn't exist
        File directory = new File(INVOICE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    public ResponseEntity<Resource> getFromCache(Long orderId) {
        String fileName = "invoice_" + orderId + ".pdf";
        String filePath = INVOICE_DIRECTORY + fileName;
        File cachedInvoice = new File(filePath);
        if (cachedInvoice.exists() && cachedInvoice.length() > 0) {
            
                // Create response with cached file
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
                headers.setContentLength(cachedInvoice.length());
                
                Resource resource = new FileSystemResource(cachedInvoice);
                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            
        }
        
        return null;
    }


    public ResponseEntity<Resource> cacheAndReturn(Long orderId, ResponseEntity<Resource> response) {
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return response;
        }
        
        String fileName = "invoice_" + orderId + ".pdf";
        String filePath = INVOICE_DIRECTORY + fileName;
        
        try {
            // Read the content from the response
            Resource responseBody = response.getBody();
            
            // Ensure the directory exists
            File directory = new File(INVOICE_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Save to file
            try (InputStream inputStream = responseBody.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(filePath)) {
                
                // Copy the stream directly to the file
                IOUtils.copy(inputStream, outputStream);
            }
            
            // Return the original response
            return response;
        } catch (IOException e) {
            // Return the original response even if caching fails
            return response;
        }
    }
} 