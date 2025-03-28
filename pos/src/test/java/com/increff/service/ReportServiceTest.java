package com.increff.service;

import com.increff.dao.ReportDao;
import com.increff.model.SalesReportData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {

    @Mock
    private ReportDao dao;

    @InjectMocks
    private ReportService service;

    @Test
    public void testGetSalesReportWithClientId() {
        // Given
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now();
        Long clientId = 1L;
        
        List<SalesReportData> expectedData = Arrays.asList(
            createSalesReportData("1234567890", "Product 1", 10, new BigDecimal("999.90")),
            createSalesReportData("0987654321", "Product 2", 5, new BigDecimal("249.95"))
        );
        
        when(dao.getSalesReport(startDate, endDate, clientId)).thenReturn(expectedData);
        
        // When
        List<SalesReportData> results = service.getSalesReport(startDate, endDate, clientId);
        
        // Then
        assertEquals(2, results.size());
        verify(dao).getSalesReport(startDate, endDate, clientId);
    }

    @Test
    public void testGetSalesReportWithoutClientId() {
        // Given
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now();
        
        List<SalesReportData> expectedData = Arrays.asList(
            createSalesReportData("1234567890", "Product 1", 10, new BigDecimal("999.90")),
            createSalesReportData("0987654321", "Product 2", 5, new BigDecimal("249.95"))
        );
        
        when(dao.getSalesReport(startDate, endDate, null)).thenReturn(expectedData);
        
        // When
        List<SalesReportData> results = service.getSalesReport(startDate, endDate, null);
        
        // Then
        assertEquals(2, results.size());
        verify(dao).getSalesReport(startDate, endDate, null);
    }

    private SalesReportData createSalesReportData(String barcode, String name, int quantity, BigDecimal revenue) {
        return new SalesReportData(barcode, name, (long)quantity, revenue);
    }
} 