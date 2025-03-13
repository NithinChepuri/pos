package com.increff.employee.dto;

import com.increff.employee.model.InvoiceDetails;
import com.increff.employee.service.PDFGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
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

    public String generateInvoice(InvoiceDetails details) throws Exception {
        if (details == null || details.getOrderId() == null) {
            throw new IllegalArgumentException("Invalid request: Order details required");
        }
        return pdfService.generateInvoice(details);
    }

    public byte[] downloadInvoice(InvoiceDetails details) throws Exception {
        if (details == null || details.getOrderId() == null) {
            throw new IllegalArgumentException("Invalid request: Order details required");
        }

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
        return Files.readAllBytes(new File(filePath).toPath());
    }
} 