package com.increff.service;

import com.increff.dao.InventoryDao;
import com.increff.entity.InventoryEntity;
import com.increff.model.inventory.InventorySearchForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class InventoryServiceTest {

    @Mock
    private InventoryDao dao;

    @InjectMocks
    private InventoryService service;

    @Test
    public void testAdd() {
        // Arrange
        InventoryEntity inventory = createInventory(1L, 100L);
        
        // Act
        service.add(inventory);
        
        // Assert
        verify(dao).insert(inventory);
    }

    @Test
    public void testAddWithNullVersion() {
        // Arrange
        InventoryEntity inventory = createInventory(1L, 100L);
        inventory.setVersion(null);
        
        // Act
        service.add(inventory);
        
        // Assert
        assertEquals(Integer.valueOf(0), inventory.getVersion());
        verify(dao).insert(inventory);
    }

    @Test
    public void testUpdate() throws ApiException {
        // Arrange
        Long id = 1L;
        Long newQuantity = 200L;
        InventoryEntity inventory = createInventory(1L, 100L);
        when(dao.select(id)).thenReturn(inventory);
        
        // Act
        service.update(id, newQuantity);
        
        // Assert
        assertEquals(newQuantity, inventory.getQuantity());
        verify(dao).update(inventory);
    }

    @Test(expected = ApiException.class)
    public void testUpdateInventoryNotFound() throws ApiException {
        // Arrange
        Long id = 1L;
        Long newQuantity = 200L;
        when(dao.select(id)).thenReturn(null);
        
        // Act
        service.update(id, newQuantity);
    }

    @Test
    public void testGet() {
        // Arrange
        Long id = 1L;
        InventoryEntity inventory = createInventory(1L, 100L);
        when(dao.select(id)).thenReturn(inventory);
        
        // Act
        InventoryEntity result = service.get(id);
        
        // Assert
        assertEquals(inventory, result);
    }

    @Test
    public void testGetAll() {
        // Arrange
        List<InventoryEntity> expectedInventories = Arrays.asList(
            createInventory(1L, 100L),
            createInventory(2L, 200L)
        );
        when(dao.selectAll()).thenReturn(expectedInventories);
        
        // Act
        List<InventoryEntity> result = service.getAll();
        
        // Assert
        assertEquals(expectedInventories, result);
    }

    @Test
    public void testGetAllWithPagination() {
        // Arrange
        int page = 0;
        int size = 10;
        List<InventoryEntity> expectedInventories = Arrays.asList(
            createInventory(1L, 100L),
            createInventory(2L, 200L)
        );
        when(dao.selectAll(page, size)).thenReturn(expectedInventories);
        
        // Act
        List<InventoryEntity> result = service.getAll(page, size);
        
        // Assert
        assertEquals(expectedInventories, result);
    }

    @Test
    public void testGetByProductId() {
        // Arrange
        Long productId = 1L;
        InventoryEntity inventory = createInventory(productId, 100L);
        when(dao.selectByProductId(productId)).thenReturn(inventory);
        
        // Act
        InventoryEntity result = service.getByProductId(productId);
        
        // Assert
        assertEquals(inventory, result);
    }

    @Test
    public void testUpdateInventoryWhenExists() throws ApiException {
        // Arrange
        Long productId = 1L;
        Long quantity = 200L;
        InventoryEntity existingInventory = createInventory(productId, 100L);
        when(dao.selectByProductId(productId)).thenReturn(existingInventory);
        when(dao.update(existingInventory)).thenReturn(existingInventory);
        
        // Act
        InventoryEntity result = service.updateInventory(productId, quantity);
        
        // Assert
        assertEquals(quantity, result.getQuantity());
        verify(dao).update(existingInventory);
    }

    @Test
    public void testUpdateInventoryWhenNotExists() throws ApiException {
        // Arrange
        Long productId = 1L;
        Long quantity = 200L;
        when(dao.selectByProductId(productId)).thenReturn(null);
        
        InventoryEntity newInventory = new InventoryEntity();
        newInventory.setProductId(productId);
        newInventory.setQuantity(quantity);
        newInventory.setVersion(0);
        
        when(dao.insert(any(InventoryEntity.class))).thenAnswer(invocation -> {
            InventoryEntity entity = invocation.getArgumentAt(0, InventoryEntity.class);
            entity.setId(1L); // Simulate ID generation
            return entity;
        });
        
        // Act
        InventoryEntity result = service.updateInventory(productId, quantity);
        
        // Assert
        assertEquals(productId, result.getProductId());
        assertEquals(quantity, result.getQuantity());
        assertEquals(Integer.valueOf(0), result.getVersion());
        verify(dao).insert(any(InventoryEntity.class));
    }

    @Test
    public void testDecreaseInventory() throws ApiException {
        // Arrange
        Long productId = 1L;
        Long decreaseAmount = -50L;
        InventoryEntity inventory = createInventory(productId, 100L);
        when(dao.selectByProductId(productId)).thenReturn(inventory);
        when(dao.update(inventory)).thenReturn(inventory);
        
        // Act
        InventoryEntity result = service.decreaseInventory(productId, decreaseAmount);
        
        // Assert
        assertEquals(Long.valueOf(50L), result.getQuantity());
        verify(dao).update(inventory);
    }

    @Test
    public void testSearchWithPagination() {
        // Arrange
        InventorySearchForm form = new InventorySearchForm();
        int page = 0;
        int size = 10;
        List<InventoryEntity> expectedInventories = Arrays.asList(
            createInventory(1L, 100L),
            createInventory(2L, 200L)
        );
        when(dao.search(form, page, size)).thenReturn(expectedInventories);
        
        // Act
        List<InventoryEntity> result = service.search(form, page, size);
        
        // Assert
        assertEquals(expectedInventories, result);
    }

    @Test
    public void testSearch() {
        // Arrange
        InventorySearchForm form = new InventorySearchForm();
        List<InventoryEntity> expectedInventories = Arrays.asList(
            createInventory(1L, 100L),
            createInventory(2L, 200L)
        );
        when(dao.search(eq(form), anyInt(), anyInt())).thenReturn(expectedInventories);
        
        // Act
        List<InventoryEntity> result = service.search(form);
        
        // Assert
        assertEquals(expectedInventories, result);
    }

    @Test
    public void testCheckInventoryWhenSufficient() {
        // Arrange
        Long productId = 1L;
        Long requiredQuantity = 50L;
        InventoryEntity inventory = createInventory(productId, 100L);
        when(dao.selectByProductId(productId)).thenReturn(inventory);
        
        // Act
        boolean result = service.checkInventory(productId, requiredQuantity);
        
        // Assert
        assertTrue(result);
    }

    @Test
    public void testCheckInventoryWhenInsufficient() {
        // Arrange
        Long productId = 1L;
        Long requiredQuantity = 150L;
        InventoryEntity inventory = createInventory(productId, 100L);
        when(dao.selectByProductId(productId)).thenReturn(inventory);
        
        // Act
        boolean result = service.checkInventory(productId, requiredQuantity);
        
        // Assert
        assertFalse(result);
    }

    @Test
    public void testCheckInventoryWhenNotExists() {
        // Arrange
        Long productId = 1L;
        Long requiredQuantity = 50L;
        when(dao.selectByProductId(productId)).thenReturn(null);
        
        // Act
        boolean result = service.checkInventory(productId, requiredQuantity);
        
        // Assert
        assertFalse(result);
    }

    @Test
    public void testExistsByProductIdWhenExists() {
        // Arrange
        Long productId = 1L;
        InventoryEntity inventory = createInventory(productId, 100L);
        when(dao.selectByProductId(productId)).thenReturn(inventory);
        
        // Act
        boolean result = service.existsByProductId(productId);
        
        // Assert
        assertTrue(result);
    }

    @Test
    public void testExistsByProductIdWhenNotExists() {
        // Arrange
        Long productId = 1L;
        when(dao.selectByProductId(productId)).thenReturn(null);
        
        // Act
        boolean result = service.existsByProductId(productId);
        
        // Assert
        assertFalse(result);
    }

    @Test
    public void testDelete() throws ApiException {
        // Arrange
        Long id = 1L;
        InventoryEntity inventory = createInventory(1L, 100L);
        when(dao.select(id)).thenReturn(inventory);
        
        // Act
        service.delete(id);
        
        // Assert
        verify(dao).delete(inventory);
    }

    @Test(expected = ApiException.class)
    public void testDeleteInventoryNotFound() throws ApiException {
        // Arrange
        Long id = 1L;
        when(dao.select(id)).thenReturn(null);
        
        // Act
        service.delete(id);
    }

    private InventoryEntity createInventory(Long productId, Long quantity) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(productId); // Using productId as ID for simplicity
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        inventory.setVersion(0);
        return inventory;
    }
} 