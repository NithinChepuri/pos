// package com.increff.dao;

// import com.increff.entity.InventoryEntity;
// import com.increff.model.inventory.InventorySearchForm;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.runners.MockitoJUnitRunner;

// import javax.persistence.EntityManager;
// import javax.persistence.TypedQuery;
// import java.util.Arrays;
// import java.util.List;

// import static org.mockito.Mockito.*;
// import static org.junit.Assert.*;

// @RunWith(MockitoJUnitRunner.class)
// public class InventoryDaoTest {

//     @Mock
//     private EntityManager em;

//     @Mock
//     private TypedQuery<InventoryEntity> query;

//     @InjectMocks
//     private InventoryDao dao;

//     @Test
//     public void testInsert() {
//         // Arrange
//         InventoryEntity inventory = createInventory(1L, 100L);
        
//         // Act
//         dao.insert(inventory);
        
//         // Assert
//         verify(em).persist(inventory);
//     }

//     @Test
//     public void testSelect() {
//         // Arrange
//         Long id = 1L;
//         InventoryEntity inventory = createInventory(1L, 100L);
//         when(em.find(InventoryEntity.class, id)).thenReturn(inventory);
        
//         // Act
//         InventoryEntity result = dao.select(id);
        
//         // Assert
//         assertEquals(inventory, result);
//     }

//     @Test
//     public void testSelectAll() {
//         // Arrange
//         List<InventoryEntity> expectedInventories = Arrays.asList(
//             createInventory(1L, 100L),
//             createInventory(2L, 200L)
//         );
        
//         when(em.createQuery(anyString(), eq(InventoryEntity.class))).thenReturn(query);
//         when(query.getResultList()).thenReturn(expectedInventories);
        
//         // Act
//         List<InventoryEntity> result = dao.selectAll();
        
//         // Assert
//         assertEquals(expectedInventories, result);
//     }

//     @Test
//     public void testSelectByProductId() {
//         // Arrange
//         Long productId = 1L;
//         InventoryEntity inventory = createInventory(productId, 100L);
//         List<InventoryEntity> inventories = Arrays.asList(inventory);
        
//         when(em.createQuery(anyString(), eq(InventoryEntity.class))).thenReturn(query);
//         when(query.setParameter("productId", productId)).thenReturn(query);
//         when(query.getResultList()).thenReturn(inventories);
        
//         // Act
//         InventoryEntity result = dao.selectByProductId(productId);
        
//         // Assert
//         assertEquals(inventory, result);
//     }

//     @Test
//     public void testSelectByProductIdWhenEmpty() {
//         // Arrange
//         Long productId = 1L;
//         List<InventoryEntity> inventories = Arrays.asList();
        
//         when(em.createQuery(anyString(), eq(InventoryEntity.class))).thenReturn(query);
//         when(query.setParameter("productId", productId)).thenReturn(query);
//         when(query.getResultList()).thenReturn(inventories);
        
//         // Act
//         InventoryEntity result = dao.selectByProductId(productId);
        
//         // Assert
//         assertNull(result);
//     }

//     @Test
//     public void testUpdate() {
//         // Arrange
//         InventoryEntity inventory = createInventory(1L, 100L);
//         when(em.merge(inventory)).thenReturn(inventory);
        
//         // Act
//         InventoryEntity result = dao.update(inventory);
        
//         // Assert
//         assertEquals(inventory, result);
//         verify(em).merge(inventory);
//     }

//     @Test
//     public void testSearch() {
//         // Arrange
//         InventorySearchForm form = new InventorySearchForm();
//         form.setBarcode("ABC");
//         form.setProductName("Test Product");
        
//         List<InventoryEntity> expectedInventories = Arrays.asList(
//             createInventory(1L, 100L),
//             createInventory(2L, 200L)
//         );
        
//         when(em.createQuery(anyString(), eq(InventoryEntity.class))).thenReturn(query);
//         when(query.setParameter(anyString(), any())).thenReturn(query);
//         when(query.setFirstResult(anyInt())).thenReturn(query);
//         when(query.setMaxResults(anyInt())).thenReturn(query);
//         when(query.getResultList()).thenReturn(expectedInventories);
        
//         // Act
//         List<InventoryEntity> result = dao.search(form, 0, 10);
        
//         // Assert
//         assertEquals(expectedInventories, result);
//     }

//     @Test
//     public void testSearchByBarcode() {
//         // Arrange
//         String barcode = "ABC";
//         List<InventoryEntity> expectedInventories = Arrays.asList(
//             createInventory(1L, 100L),
//             createInventory(2L, 200L)
//         );
        
//         when(em.createQuery(anyString(), eq(InventoryEntity.class))).thenReturn(query);
//         when(query.setParameter("barcode", "%" + barcode + "%")).thenReturn(query);
//         when(query.setFirstResult(anyInt())).thenReturn(query);
//         when(query.setMaxResults(anyInt())).thenReturn(query);
//         when(query.getResultList()).thenReturn(expectedInventories);
        
//         // Act
//         List<InventoryEntity> result = dao.searchByBarcode(barcode, 0, 10);
        
//         // Assert
//         assertEquals(expectedInventories, result);
//     }

//     @Test
//     public void testSearchByProductName() {
//         // Arrange
//         String productName = "Test Product";
//         List<InventoryEntity> expectedInventories = Arrays.asList(
//             createInventory(1L, 100L),
//             createInventory(2L, 200L)
//         );
        
//         when(em.createQuery(anyString(), eq(InventoryEntity.class))).thenReturn(query);
//         when(query.setParameter("productName", "%" + productName + "%")).thenReturn(query);
//         when(query.setFirstResult(anyInt())).thenReturn(query);
//         when(query.setMaxResults(anyInt())).thenReturn(query);
//         when(query.getResultList()).thenReturn(expectedInventories);
        
//         // Act
//         List<InventoryEntity> result = dao.searchByProductName(productName, 0, 10);
        
//         // Assert
//         assertEquals(expectedInventories, result);
//     }

//     @Test
//     public void testSearchByBarcodeOrProductName() {
//         // Arrange
//         String barcode = "ABC";
//         String productName = "Test Product";
//         List<InventoryEntity> expectedInventories = Arrays.asList(
//             createInventory(1L, 100L),
//             createInventory(2L, 200L)
//         );
        
//         when(em.createQuery(anyString(), eq(InventoryEntity.class))).thenReturn(query);
//         when(query.setParameter("barcode", "%" + barcode + "%")).thenReturn(query);
//         when(query.setParameter("productName", "%" + productName + "%")).thenReturn(query);
//         when(query.setFirstResult(anyInt())).thenReturn(query);
//         when(query.setMaxResults(anyInt())).thenReturn(query);
//         when(query.getResultList()).thenReturn(expectedInventories);
        
//         // Act
//         List<InventoryEntity> result = dao.searchByBarcodeOrProductName(barcode, productName, 0, 10);
        
//         // Assert
//         assertEquals(expectedInventories, result);
//     }

//     @Test
//     public void testSelectAllWithPagination() {
//         // Arrange
//         List<InventoryEntity> expectedInventories = Arrays.asList(
//             createInventory(1L, 100L),
//             createInventory(2L, 200L)
//         );
        
//         when(em.createQuery(anyString(), eq(InventoryEntity.class))).thenReturn(query);
//         when(query.setFirstResult(anyInt())).thenReturn(query);
//         when(query.setMaxResults(anyInt())).thenReturn(query);
//         when(query.getResultList()).thenReturn(expectedInventories);
        
//         // Act
//         List<InventoryEntity> result = dao.selectAll(0, 10);
        
//         // Assert
//         assertEquals(expectedInventories, result);
//     }

//     @Test
//     public void testDelete() {
//         // Arrange
//         InventoryEntity inventory = createInventory(1L, 100L);
//         when(em.contains(inventory)).thenReturn(true);
        
//         // Act
//         dao.delete(inventory);
        
//         // Assert
//         verify(em).remove(inventory);
//     }

//     @Test
//     public void testDeleteWhenNotManaged() {
//         // Arrange
//         InventoryEntity inventory = createInventory(1L, 100L);
//         InventoryEntity mergedInventory = createInventory(1L, 100L);
        
//         when(em.contains(inventory)).thenReturn(false);
//         when(em.merge(inventory)).thenReturn(mergedInventory);
        
//         // Act
//         dao.delete(inventory);
        
//         // Assert
//         verify(em).merge(inventory);
//         verify(em).remove(mergedInventory);
//     }

//     private InventoryEntity createInventory(Long productId, Long quantity) {
//         InventoryEntity inventory = new InventoryEntity();
//         inventory.setId(productId); // Using productId as ID for simplicity
//         inventory.setProductId(productId);
//         inventory.setQuantity(quantity);
//         return inventory;
//     }
// } 