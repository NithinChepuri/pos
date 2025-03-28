package com.increff.dto;

import com.increff.model.SalesReportData;
import com.increff.model.SalesReportForm;
import com.increff.service.ApiException;
import com.increff.service.ReportService;
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
public class ReportDtoTest {

    @Mock
    private ReportService service;

    @InjectMocks
    private ReportDto dto;

    @Test
    public void testGetSalesReport() throws ApiException {
        // Given
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now().minusDays(1);
        Long clientId = 1L;
        
        List<SalesReportData> expectedData = Arrays.asList(
            createSalesReportData("1234567890", "Product 1", 10, new BigDecimal("999.90")),
            createSalesReportData("0987654321", "Product 2", 5, new BigDecimal("249.95"))
        );
        
        when(service.getSalesReport(startDate, endDate, clientId)).thenReturn(expectedData);
        
        // When
        List<SalesReportData> results = dto.getSalesReport(startDate, endDate, clientId);
        
        // Then
        assertEquals(2, results.size());
        verify(service).getSalesReport(startDate, endDate, clientId);
    }

    @Test(expected = ApiException.class)
    public void testGetSalesReportWithInvalidDateRange() throws ApiException {
        // Given
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(ZonedDateTime.now().minusDays(1));
        form.setEndDate(ZonedDateTime.now().minusDays(7));
        
        // When/Then
        dto.getSalesReport(form);
    }

    @Test(expected = ApiException.class)
    public void testGetSalesReportWithFutureEndDate() throws ApiException {
        // Given
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(ZonedDateTime.now().minusDays(7));
        form.setEndDate(ZonedDateTime.now().plusDays(1));
        
        // When/Then
        dto.getSalesReport(form);
    }

    @Test(expected = ApiException.class)
    public void testGetSalesReportWithNullDates() throws ApiException {
        // Given
        SalesReportForm form = new SalesReportForm();
        
        // When/Then
        dto.getSalesReport(form);
    }

    private SalesReportData createSalesReportData(String barcode, String name, int quantity, BigDecimal revenue) {
        return new SalesReportData(barcode, name, (long)quantity, revenue);
    }
} 