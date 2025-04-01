package com.increff.service.client;

import com.increff.model.invoice.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client service for interacting with the external Invoice Generation Service.
 */
@Service
public class InvoiceClientService {

    @Autowired
    private RestTemplate restTemplate;


    public ResponseEntity<byte[]> generateInvoicePdf(String invoiceServiceUrl, InvoiceData invoiceData) throws RestClientException {
        // Create HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HTTP entity with headers and body
        HttpEntity<InvoiceData> requestEntity = new HttpEntity<>(invoiceData, headers);

        // Make the request
        return restTemplate.exchange(
                invoiceServiceUrl,
                HttpMethod.POST,
                requestEntity,
                byte[].class
        );
    }
} 