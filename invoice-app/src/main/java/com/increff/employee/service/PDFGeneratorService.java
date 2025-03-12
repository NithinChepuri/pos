package com.increff.employee.service;

import com.increff.employee.model.InvoiceDetails;
import com.increff.employee.model.InvoiceItemDetails;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class PDFGeneratorService {

    public String generateInvoice(InvoiceDetails details) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        try {
            document.open();
            addHeader(document, details);
            addItems(document, details);
            addTotal(document, details);
        } finally {
            document.close();
        }

        byte[] pdfBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(pdfBytes);
    }

    private void addHeader(Document document, InvoiceDetails details) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        Paragraph orderInfo = new Paragraph();
        orderInfo.add(new Chunk("Order ID: " + details.getOrderId() + "\n", normalFont));
        orderInfo.add(new Chunk("Date: " + details.getOrderDate().format(
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")), normalFont));
        document.add(orderInfo);
        document.add(Chunk.NEWLINE);
    }

    private void addItems(Document document, InvoiceDetails details) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 1, 2, 2});

        // Add Headers
        addTableHeader(table);

        // Add Items
        for (InvoiceItemDetails item : details.getItems()) {
            table.addCell(item.getName());
            table.addCell(item.getBarcode());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("₹%.2f", item.getUnitPrice()));
            table.addCell(String.format("₹%.2f", item.getAmount()));
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table) {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        
        Arrays.asList("Product", "Barcode", "Quantity", "Unit Price", "Total")
            .forEach(columnTitle -> {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(2);
                header.setPhrase(new Phrase(columnTitle, headerFont));
                header.setPadding(5);
                table.addCell(header);
            });
    }

    private void addTotal(Document document, InvoiceDetails details) throws DocumentException {
        document.add(Chunk.NEWLINE);
        Paragraph total = new Paragraph();
        total.add(new Chunk("Total Amount: ₹" + 
            String.format("%.2f", details.getTotal()), 
            new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);
    }
} 