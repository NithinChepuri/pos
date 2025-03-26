// package com.increff.dao;

// import com.increff.entity.DailySalesEntity;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.runners.MockitoJUnitRunner;

// import javax.persistence.EntityManager;
// import javax.persistence.NoResultException;
// import javax.persistence.TypedQuery;
// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import static org.mockito.Mockito.*;
// import static org.junit.Assert.*;

// @RunWith(MockitoJUnitRunner.class)
// public class DailySalesDaoTest {

//     @Mock
//     private EntityManager em;

//     @Mock
//     private TypedQuery<DailySalesEntity> query;

//     @InjectMocks
//     private DailySalesDao dao;

//     @Test
//     public void testInsert() {
//         // Arrange
//         DailySalesEntity entity = createDailySalesEntity(LocalDate.now());
        
//         // Act
//         dao.insert(entity);
        
//         // Assert
//         verify(em).persist(entity);
//     }

//     @Test
//     public void testSelectByDateWhenExists() {
//         // Arrange
//         LocalDate date = LocalDate.now();
//         DailySalesEntity entity = createDailySalesEntity(date);
        
//         when(em.createQuery(anyString(), eq(DailySalesEntity.class))).thenReturn(query);
//         when(query.setParameter("date", date)).thenReturn(query);
//         when(query.getSingleResult()).thenReturn(entity);
        
//         // Act
//         Optional<DailySalesEntity> result = dao.selectByDate(date);
        
//         // Assert
//         assertTrue(result.isPresent());
//         assertEquals(entity, result.get());
//     }

//     @Test
//     public void testSelectByDateWhenNotExists() {
//         // Arrange
//         LocalDate date = LocalDate.now();
        
//         when(em.createQuery(anyString(), eq(DailySalesEntity.class))).thenReturn(query);
//         when(query.setParameter("date", date)).thenReturn(query);
//         when(query.getSingleResult()).thenThrow(new NoResultException());
        
//         // Act
//         Optional<DailySalesEntity> result = dao.selectByDate(date);
        
//         // Assert
//         assertFalse(result.isPresent());
//     }

//     @Test
//     public void testSelectByDateRange() {
//         // Arrange
//         LocalDate startDate = LocalDate.now().minusDays(7);
//         LocalDate endDate = LocalDate.now();
        
//         List<DailySalesEntity> entities = Arrays.asList(
//             createDailySalesEntity(startDate),
//             createDailySalesEntity(endDate)
//         );
        
//         when(em.createQuery(anyString(), eq(DailySalesEntity.class))).thenReturn(query);
//         when(query.setParameter("startDate", startDate)).thenReturn(query);
//         when(query.setParameter("endDate", endDate)).thenReturn(query);
//         when(query.getResultList()).thenReturn(entities);
        
//         // Act
//         List<DailySalesEntity> results = dao.selectByDateRange(startDate, endDate);
        
//         // Assert
//         assertEquals(2, results.size());
//     }

//     @Test
//     public void testSelectLatestWhenExists() {
//         // Arrange
//         DailySalesEntity entity = createDailySalesEntity(LocalDate.now());
        
//         when(em.createQuery(anyString(), eq(DailySalesEntity.class))).thenReturn(query);
//         when(query.setMaxResults(1)).thenReturn(query);
//         when(query.getSingleResult()).thenReturn(entity);
        
//         // Act
//         Optional<DailySalesEntity> result = dao.selectLatest();
        
//         // Assert
//         assertTrue(result.isPresent());
//         assertEquals(entity, result.get());
//     }

//     @Test
//     public void testSelectLatestWhenNotExists() {
//         // Arrange
//         when(em.createQuery(anyString(), eq(DailySalesEntity.class))).thenReturn(query);
//         when(query.setMaxResults(1)).thenReturn(query);
//         when(query.getSingleResult()).thenThrow(new NoResultException());
        
//         // Act
//         Optional<DailySalesEntity> result = dao.selectLatest();
        
//         // Assert
//         assertFalse(result.isPresent());
//     }

//     private DailySalesEntity createDailySalesEntity(LocalDate date) {
//         DailySalesEntity entity = new DailySalesEntity();
//         entity.setDate(date);
//         entity.setTotalOrders(10);
//         entity.setTotalItems(50);
//         entity.setTotalRevenue(new BigDecimal("1000.00"));
//         entity.setInvoicedOrderCount(8);
//         entity.setInvoicedItemCount(40);
//         return entity;
//     }
// } 