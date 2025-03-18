package com.increff.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.increff.dao.InventoryDao;
import com.increff.dao.ProductDao;
import com.increff.entity.InventoryEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.InventoryForm;

import java.math.BigDecimal;
import java.util.List;

public class InventoryServiceTest extends AbstractUnitTest {

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ProductDao productDao;
    
    @Autowired
    private InventoryDao inventoryDao;

    @Test
    public void testAdd() {
        // Create and add inventory
        InventoryEntity inventory = createInventory(1L, 100L);
        inventoryService.add(inventory);
        
        // Verify
        InventoryEntity fetched = inventoryService.get(inventory.getId());
        assertNotNull(fetched);
        assertEquals(inventory.getProductId(), fetched.getProductId());
        assertEquals(inventory.getQuantity(), fetched.getQuantity());
    }
    
    @Test
    public void testUpdate() throws ApiException {
        // Create and add inventory
        InventoryEntity inventory = createInventory(1L, 100L);
        inventoryService.add(inventory);
        
        // Update quantity
        Long newQuantity = 200L;
        inventoryService.update(inventory.getId(), newQuantity);
        
        // Verify
        InventoryEntity updated = inventoryService.get(inventory.getId());
        assertEquals(newQuantity, updated.getQuantity());
    }
    
    @Test(expected = ApiException.class)
    public void testUpdateNonExistentInventory() throws ApiException {
        // Try to update non-existent inventory
        inventoryService.update(999L, 100L);
    }
    
    @Test
    public void testGetAll() {
        // Create and add multiple inventory items
        InventoryEntity inventory1 = createInventory(1L, 100L);
        InventoryEntity inventory2 = createInventory(2L, 200L);
        inventoryService.add(inventory1);
        inventoryService.add(inventory2);
        
        // Get all inventory
        List<InventoryEntity> inventoryList = inventoryService.getAll();
        
        // Verify
        assertNotNull(inventoryList);
        assertEquals(2, inventoryList.size());
    }
    
    @Test
    public void testGetByProductId() {
        // Create and add inventory
        Long productId = 1L;
        InventoryEntity inventory = createInventory(productId, 100L);
        inventoryService.add(inventory);
        
        // Get by product ID
        InventoryEntity fetched = inventoryService.getByProductId(productId);
        
        // Verify
        assertNotNull(fetched);
        assertEquals(productId, fetched.getProductId());
    }
    
    @Test
    public void testUpdateInventoryExisting() throws ApiException {
        // Create and add inventory
        Long productId = 1L;
        InventoryEntity inventory = createInventory(productId, 100L);
        inventoryService.add(inventory);
        
        // Update inventory
        Long change = 50L;
        inventoryService.updateInventory(productId, change);
        
        // Verify
        InventoryEntity updated = inventoryService.getByProductId(productId);
        assertEquals(Long.valueOf(150L), updated.getQuantity());
    }
    
    @Test
    public void testUpdateInventoryNew() throws ApiException {
        // Create product ID that doesn't have inventory yet
        Long productId = 99L;
        
        // Update inventory (should create new)
        Long quantity = 75L;
        inventoryService.updateInventory(productId, quantity);
        
        // Verify
        InventoryEntity created = inventoryService.getByProductId(productId);
        assertNotNull(created);
        assertEquals(productId, created.getProductId());
        assertEquals(quantity, created.getQuantity());
    }
    
    @Test
    public void testSearch() {
        // Create product entities with names and barcodes
        ProductEntity product1 = createProduct("Apple", "apple123", 1L);
        ProductEntity product2 = createProduct("Banana", "banana456", 2L);
        productDao.insert(product1);
        productDao.insert(product2);
        
        // Create inventory items linked to these products
        InventoryEntity inventory1 = createInventory(product1.getId(), 100L);
        InventoryEntity inventory2 = createInventory(product2.getId(), 200L);
        inventoryService.add(inventory1);
        inventoryService.add(inventory2);
        
        // Search by barcode
        InventoryForm barcodeForm = new InventoryForm();
        barcodeForm.setBarcode("apple123");
        List<InventoryEntity> barcodeResults = inventoryService.search(barcodeForm);
        
        // Verify barcode search
        assertEquals(1, barcodeResults.size());
        assertEquals(product1.getId(), barcodeResults.get(0).getProductId());
        
        // Search by product name
        InventoryForm nameForm = new InventoryForm();
        nameForm.setProductName("Banana");
        List<InventoryEntity> nameResults = inventoryService.search(nameForm);
        
        // Verify name search
        assertEquals(1, nameResults.size());
        assertEquals(product2.getId(), nameResults.get(0).getProductId());
        
        // Search with empty form (should return all)
        InventoryForm emptyForm = new InventoryForm();
        List<InventoryEntity> allResults = inventoryService.search(emptyForm);
        
        // Verify empty search
        assertEquals(2, allResults.size());
    }
    
    @Test
    public void testCheckInventorySufficient() {
        // Create and add inventory with sufficient quantity
        Long productId = 1L;
        Long quantity = 100L;
        InventoryEntity inventory = createInventory(productId, quantity);
        inventoryService.add(inventory);
        
        // Check if inventory is sufficient
        boolean sufficient = inventoryService.checkInventory(productId, 50L);
        
        // Verify
        assertTrue(sufficient);
    }
    
    @Test
    public void testCheckInventoryInsufficient() {
        // Create and add inventory with insufficient quantity
        Long productId = 1L;
        Long quantity = 30L;
        InventoryEntity inventory = createInventory(productId, quantity);
        inventoryService.add(inventory);
        
        // Check if inventory is sufficient
        boolean sufficient = inventoryService.checkInventory(productId, 50L);
        
        // Verify
        assertFalse(sufficient);
    }
    
    @Test
    public void testCheckInventoryNonExistent() {
        // Check inventory for non-existent product
        boolean sufficient = inventoryService.checkInventory(999L, 10L);
        
        // Verify
        assertFalse(sufficient);
    }
    
    // Helper method to create inventory entity
    private InventoryEntity createInventory(Long productId, Long quantity) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        return inventory;
    }
    
    // Helper method to create product entity
    private ProductEntity createProduct(String name, String barcode, Long clientId) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setBarcode(barcode);
        product.setClientId(clientId);
        product.setMrp(BigDecimal.TEN);
        return product;
    }
}
