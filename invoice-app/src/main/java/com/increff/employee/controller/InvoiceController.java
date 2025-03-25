package com.increff.employee.controller;

import com.increff.employee.dto.InvoiceDto;
import com.increff.employee.model.InvoiceDetails;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Api
@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @ApiOperation(value = "Generate Invoice PDF")
    @PostMapping("/generate")
    public ResponseEntity<?> generateInvoice(@RequestBody InvoiceDetails details) {
        return invoiceDto.generateInvoice(details);
    }

    @ApiOperation(value = "Download Invoice PDF")
    @PostMapping("/download")
    public ResponseEntity<?> downloadInvoice(@RequestBody InvoiceDetails details) {
        return invoiceDto.downloadInvoice(details);
    }
} 