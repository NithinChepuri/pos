package com.increff.dao;

import com.increff.model.SalesReportData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportDaoTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<SalesReportData> query;

    @InjectMocks
    private ReportDao dao;

    @Test
    public void testGetSalesReportWithClientId() {
        // Arrange
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now();
        Long clientId = 1L;
        
        List<SalesReportData> expectedData = Arrays.asList(
            createSalesReportData("1234567890", "Product 1", 10, new BigDecimal("999.90")),
            createSalesReportData("0987654321", "Product 2", 5, new BigDecimal("249.95"))
        );
        
        when(em.createQuery(anyString(), eq(SalesReportData.class))).thenReturn(query);
        when(query.setParameter("startDate", startDate)).thenReturn(query);
        when(query.setParameter("endDate", endDate)).thenReturn(query);
        when(query.setParameter("clientId", clientId)).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedData);
        
        // Act
        List<SalesReportData> results = dao.getSalesReport(startDate, endDate, clientId);
        
        // Assert
        assertEquals(2, results.size());
        verify(query).setParameter("clientId", clientId);
        verify(query).setParameter("startDate", startDate);
        verify(query).setParameter("endDate", endDate);
    }

    @Test
    public void testGetSalesReportWithoutClientId() {
        // Arrange
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now();
        
        List<SalesReportData> expectedData = Arrays.asList(
            createSalesReportData("1234567890", "Product 1", 10, new BigDecimal("999.90")),
            createSalesReportData("0987654321", "Product 2", 5, new BigDecimal("249.95"))
        );
        
        when(em.createQuery(anyString(), eq(SalesReportData.class))).thenReturn(query);
        when(query.setParameter("startDate", startDate)).thenReturn(query);
        when(query.setParameter("endDate", endDate)).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedData);
        
        // Act
        List<SalesReportData> results = dao.getSalesReport(startDate, endDate, null);
        
        // Assert
        assertEquals(2, results.size());
        verify(query, never()).setParameter(eq("clientId"), any());
        verify(query).setParameter("startDate", startDate);
        verify(query).setParameter("endDate", endDate);
    }

    private SalesReportData createSalesReportData(String barcode, String name, int quantity, BigDecimal revenue) {
        return new SalesReportData(barcode, name, (long)quantity, revenue);
    }
} 