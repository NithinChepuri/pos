// package com.increff.dto;

// import com.increff.model.sales.DailySalesData;
// import com.increff.spring.AbstractUnitTest;
// import org.junit.Test;
// import org.springframework.beans.factory.annotation.Autowired;

// import java.time.LocalDate;
// import java.util.List;

// import static org.junit.Assert.*;

// public class DailySalesDtoTest extends AbstractUnitTest {

//     @Autowired
//     private DailySalesDto dailySalesDto;

//     @Test
//     public void testGetLatest() {
//         // When
//         try {
//             DailySalesData result = dailySalesDto.getLatest();
            
//             // Then
//             assertNotNull(result);
//         } catch (RuntimeException e) {
//             // This might happen if no sales data exists yet
//             assertTrue(e.getMessage().contains("No sales data found"));
//         }
//     }

//     @Test
//     public void testGetByDate() {
//         // Given
//         LocalDate yesterday = LocalDate.now().minusDays(1);
        
//         try {
//             // When
//             DailySalesData result = dailySalesDto.getByDate(yesterday);
            
//             // Then
//             assertNotNull(result);
//             assertEquals(yesterday.atStartOfDay(result.getDate().getZone()).toLocalDate(), result.getDate().toLocalDate());
//         } catch (RuntimeException e) {
//             // This might happen if no sales data exists for yesterday
//             assertTrue(e.getMessage().contains("No sales data found"));
//         }
//     }

//     @Test
//     public void testGetByDateRange() {
//         // Given
//         LocalDate startDate = LocalDate.now().minusDays(7);
//         LocalDate endDate = LocalDate.now().minusDays(1);
        
//         // When
//         List<DailySalesData> results = dailySalesDto.getByDateRange(startDate, endDate);
        
//         // Then
//         assertNotNull(results);
//         // The list might be empty if no sales data exists in the range
//     }

//     @Test(expected = RuntimeException.class)
//     public void testGetByDateRangeInvalidRange() {
//         // Given
//         LocalDate startDate = LocalDate.now();
//         LocalDate endDate = LocalDate.now().minusDays(1); // End before start
        
//         // When - should throw RuntimeException
//         dailySalesDto.getByDateRange(startDate, endDate);
//     }

//     @Test(expected = RuntimeException.class)
//     public void testGetByDateRangeFutureDates() {
//         // Given
//         LocalDate startDate = LocalDate.now();
//         LocalDate endDate = LocalDate.now().plusDays(1); // Future date
        
//         // When - should throw RuntimeException
//         dailySalesDto.getByDateRange(startDate, endDate);
//     }

//     @Test(expected = RuntimeException.class)
//     public void testGetByDateRangeTooLong() {
//         // Given
//         LocalDate startDate = LocalDate.now().minusDays(100);
//         LocalDate endDate = LocalDate.now();
        
//         // When - should throw RuntimeException
//         dailySalesDto.getByDateRange(startDate, endDate);
//     }
// } 