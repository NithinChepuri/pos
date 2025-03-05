package com.increff.employee.controller;

import com.increff.employee.model.InvoiceDetails;
import com.increff.employee.service.PDFGeneratorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

@Api
@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    private static final String PDF_DIRECTORY = "generated_invoices/";

    @Autowired
    private PDFGeneratorService pdfService;

    public InvoiceController() {
        // Create directory if it doesn't exist
        new File(PDF_DIRECTORY).mkdirs();
    }

    @ApiOperation(value = "Generate Invoice PDF")
    @PostMapping("/generate")
    public ResponseEntity<?> generateInvoice(@RequestBody InvoiceDetails details) {
        try {
            if (details == null || details.getOrderId() == null) {
                return ResponseEntity.badRequest().body("Invalid request: Order details required");
            }
            String base64PDF = pdfService.generateInvoice(details);
            return ResponseEntity.ok(base64PDF);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating PDF: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Download Invoice PDF")
    @PostMapping("/download")
    public ResponseEntity<?> downloadInvoice(@RequestBody InvoiceDetails details) {
        try {
            if (details == null || details.getOrderId() == null) {
                return ResponseEntity.badRequest().body("Invalid request: Order details required");
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
            byte[] contents = Files.readAllBytes(new File(filePath).toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + fileName + "\"");
            headers.setContentLength(contents.length);

            return new ResponseEntity<>(contents, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating PDF: " + e.getMessage());
        }
    }
} 