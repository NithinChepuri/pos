package com.increff.service;

import com.increff.dao.DailySalesDao;
import com.increff.entity.DailySalesEntity;
import com.increff.model.sales.DailySalesData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DailySalesServiceTest {

    @Mock
    private DailySalesDao dao;

    @InjectMocks
    private DailySalesService service;

    @Test
    public void testGetByDate() throws ApiException {
        // Arrange
        LocalDate date = LocalDate.now();
        DailySalesEntity entity = createDailySalesEntity(date);
        when(dao.selectByDate(date)).thenReturn(Optional.of(entity));
        
        // Act
        DailySalesData result = service.getByDate(date);
        
        // Assert
        assertNotNull(result);
        assertEquals(date.atStartOfDay(result.getDate().getZone()).toLocalDate(), result.getDate().toLocalDate());
        assertEquals(Integer.valueOf(10), result.getTotalOrders());
    }

    @Test(expected = ApiException.class)
    public void testGetByDateNotFound() throws ApiException {
        // Arrange
        LocalDate date = LocalDate.now();
        when(dao.selectByDate(date)).thenReturn(Optional.empty());
        
        // Act - should throw ApiException
        service.getByDate(date);
    }

    @Test
    public void testGetByDateRange() throws ApiException {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        List<DailySalesEntity> entities = Arrays.asList(
            createDailySalesEntity(startDate),
            createDailySalesEntity(endDate)
        );
        
        when(dao.selectByDateRange(startDate, endDate)).thenReturn(entities);
        
        // Act
        List<DailySalesData> results = service.getByDateRange(startDate, endDate);
        
        // Assert
        assertEquals(2, results.size());
    }

    @Test
    public void testGetLatest() throws ApiException {
        // Arrange
        LocalDate date = LocalDate.now();
        DailySalesEntity entity = createDailySalesEntity(date);
        when(dao.selectLatest()).thenReturn(Optional.of(entity));
        
        // Act
        DailySalesData result = service.getLatest();
        
        // Assert
        assertNotNull(result);
        assertEquals(date.atStartOfDay(result.getDate().getZone()).toLocalDate(), result.getDate().toLocalDate());
    }

    @Test(expected = ApiException.class)
    public void testGetLatestNotFound() throws ApiException {
        // Arrange
        when(dao.selectLatest()).thenReturn(Optional.empty());
        
        // Act - should throw ApiException
        service.getLatest();
    }

    private DailySalesEntity createDailySalesEntity(LocalDate date) {
        DailySalesEntity entity = new DailySalesEntity();
        entity.setDate(date);
        entity.setTotalOrders(10);
        entity.setTotalItems(50);
        entity.setTotalRevenue(new BigDecimal("1000.00"));
        entity.setInvoicedOrderCount(8);
        entity.setInvoicedItemCount(40);
        return entity;
    }
} 