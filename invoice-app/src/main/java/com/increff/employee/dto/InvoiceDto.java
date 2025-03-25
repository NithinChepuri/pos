package com.increff.employee.dto;

import com.increff.employee.model.InvoiceDetails;
import com.increff.employee.service.PDFGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Component
public class InvoiceDto {

    private static final String PDF_DIRECTORY = "generated_invoices/";

    @Autowired
    private PDFGeneratorService pdfService;

    public InvoiceDto() {
        // Create directory if it doesn't exist
        new File(PDF_DIRECTORY).mkdirs();
    }

    public ResponseEntity<?> generateInvoice(InvoiceDetails details) {
        try {
            validateInvoiceDetails(details);
            String base64PDF = pdfService.generateInvoice(details);
            return ResponseEntity.ok(base64PDF);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating PDF: " + e.getMessage());
        }
    }

    public ResponseEntity<?> downloadInvoice(InvoiceDetails details) {
        try {
            validateInvoiceDetails(details);

            // Generate PDF and save to file
            String fileName = "invoice_" + details.getOrderId() + ".pdf";
            String filePath = PDF_DIRECTORY + fileName;
            
            // Get PDF bytes
            String base64PDF = pdfService.generateInvoice(details);
            byte[] pdfBytes = Base64.getDecoder().decode(base64PDF);
            
            // Save to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }

            // Read file for response
            byte[] contents = Files.readAllBytes(new File(filePath).toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + fileName + "\"");
            headers.setContentLength(contents.length);

            return new ResponseEntity<>(contents, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error handling PDF file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating PDF: " + e.getMessage());
        }
    }

    private void validateInvoiceDetails(InvoiceDetails details) {
        if (details == null || details.getOrderId() == null) {
            throw new IllegalArgumentException("Invalid request: Order details required");
        }
    }
} 