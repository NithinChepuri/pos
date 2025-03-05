package com.increff.employee.service;

import com.increff.employee.model.InvoiceDetails;
import org.springframework.stereotype.Service;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.increff.employee.model.OrderData;
import com.increff.employee.model.OrderItemData;
import java.util.List;

@Service
public class PDFGeneratorService {
    
    private static final String PDF_STORAGE_DIR = "invoices/";

    public String generateInvoice(InvoiceDetails details) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Add content
        addHeader(document, details);
        addOrderItems(document, details.getOrderItems());
        
        document.close();
        
        // Save PDF locally
        String fileName = "invoice_" + details.getOrder().getId() + "_" + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
        String filePath = PDF_STORAGE_DIR + fileName;
        
        // Ensure directory exists
        new File(PDF_STORAGE_DIR).mkdirs();
        
        // Save file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(baos.toByteArray());
        }
        
        // Return Base64 encoded string
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
    
    private void addHeader(Document document, InvoiceDetails details) throws DocumentException {
        Paragraph header = new Paragraph("INVOICE", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        document.add(Chunk.NEWLINE);
        
        // Add order details
        document.add(new Paragraph("Order #: " + details.getOrder().getId()));
        document.add(new Paragraph("Date: " + details.getOrder().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        document.add(new Paragraph("Status: " + details.getOrder().getStatus()));
        document.add(Chunk.NEWLINE);
    }
    
    private void addOrderItems(Document document, List<OrderItemData> items) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        
        // Add headers
        table.addCell(new PdfPCell(new Phrase("Product", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Quantity", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Price", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Total", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
        
        double totalAmount = 0.0;
        // Add items
        for (OrderItemData item : items) {
            table.addCell(item.getProductName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("₹%.2f", item.getSellingPrice()));
            double itemTotal = item.getQuantity() * item.getSellingPrice();
            table.addCell(String.format("₹%.2f", itemTotal));
            totalAmount += itemTotal;
        }
        
        document.add(table);
        document.add(Chunk.NEWLINE);
        
        // Add total
        Paragraph total = new Paragraph(
            "Total Amount: ₹" + String.format("%.2f", totalAmount),
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)
        );
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);
    }
} 