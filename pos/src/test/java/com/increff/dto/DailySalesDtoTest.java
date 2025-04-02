 package com.increff.dto;

 import com.increff.model.dailySales.DailySalesData;
 import com.increff.service.ApiException;
 import com.increff.spring.AbstractUnitTest;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;

 import java.time.LocalDate;
 import java.util.List;

 import static org.junit.Assert.*;

 public class DailySalesDtoTest extends AbstractUnitTest {

     @Autowired
     private DailySalesDto dailySalesDto;

     @Test
     public void testGetLatest(){
         try {
             DailySalesData result = dailySalesDto.getLatest();

             // Then
             assertNotNull(result);
         } catch (ApiException e) {
             // This might happen if no sales data exists yet
             assertTrue(e.getMessage().contains("No sales data found"));
         }
     }
     @Test
     public void testGetByDate(){
         LocalDate ystdy  = LocalDate.now().minusDays(1);
         try{
             DailySalesData result  = dailySalesDto.getByDate(ystdy);
             assertNotNull(result);
             assertEquals(ystdy.atStartOfDay(result.getDate().getZone()).toLocalDate(),result.getDate().toLocalDate());
         }catch (ApiException e){
             assertTrue(e.getMessage().contains("No sales data found"));
         }
     }

     @Test
     public void testGetByDateRange(){

         LocalDate startDate = LocalDate.now().minusDays(7);
         LocalDate endDate = LocalDate.now().minusDays(1);

         // When
         List<DailySalesData> results = dailySalesDto.getByDateRange(startDate, endDate);

         // Then
         assertNotNull(results);
     }
//
     @Test(expected = ApiException.class)
     public void testGetByDateRangeInvalidRange() {
         // Given
         LocalDate startDate = LocalDate.now();
         LocalDate endDate = LocalDate.now().minusDays(1); // End before start

         // When - should throw RuntimeException
         dailySalesDto.getByDateRange(startDate, endDate);
     }
//
     @Test(expected = ApiException.class)
     public void testGetByDateRangeFutureDates() {
         // Given
         LocalDate startDate = LocalDate.now();
         LocalDate endDate = LocalDate.now().plusDays(1); // Future date

         // When - should throw RuntimeException
         dailySalesDto.getByDateRange(startDate, endDate);
     }
     @Test(expected = ApiException.class)
     public void testGetByDateRangeTooLong() {
         // Given
         LocalDate startDate = LocalDate.now().minusDays(100);
         LocalDate endDate = LocalDate.now();

         // When - should throw RuntimeException
         dailySalesDto.getByDateRange(startDate, endDate);
     }
 }