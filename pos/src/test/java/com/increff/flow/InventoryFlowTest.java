package com.increff.flow;

import com.increff.entity.InventoryEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryUploadForm;
import com.increff.service.ApiException;
import com.increff.service.InventoryService;
import com.increff.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class InventoryFlowTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private InventoryFlow flow;

    @Test
    public void testProcessInventoryForm() throws ApiException {
        // Arrange
        String barcode = "1234";
        String quantity = "100";
        int lineNumber = 2;
        
        InventoryUploadForm form = new InventoryUploadForm();
        form.setBarcode(barcode);
        form.setQuantity(quantity);

        ProductEntity product = createProduct(1L, "Test Product", barcode);
        when(productService.getByBarcode(barcode)).thenReturn(product);
        
        // Create a mock inventory entity to return
        InventoryEntity updatedInventory = new InventoryEntity();
        updatedInventory.setId(1L);
        updatedInventory.setProductId(product.getId());
        updatedInventory.setQuantity(100L);
        
        when(inventoryService.updateInventory(product.getId(), 100L)).thenReturn(updatedInventory);

        // Act
        InventoryData result = flow.processInventoryForm(form, lineNumber);

        // Assert
        assertNotNull(result);
        assertEquals(product.getId(), result.getProductId());
        assertEquals(Long.valueOf(100L), result.getQuantity());
        assertEquals(product.getName(), result.getProductName());
        assertEquals(product.getBarcode(), result.getBarcode());
        
        verify(productService).getByBarcode(barcode);
        verify(inventoryService).updateInventory(product.getId(), 100L);
    }

    @Test(expected = ApiException.class)
    public void testProcessInventoryFormProductNotFound() throws ApiException {
        // Arrange
        String barcode = "1234567890";
        String quantityStr = "100";
        int lineNumber = 2;

        InventoryUploadForm form = new InventoryUploadForm();
        form.setBarcode(barcode);
        form.setQuantity(quantityStr);

        when(productService.getByBarcode(barcode)).thenReturn(null);

        // Act - should throw ApiException
        flow.processInventoryForm(form, lineNumber);
    }

    @Test(expected = ApiException.class)
    public void testProcessInventoryFormInvalidQuantity() throws ApiException {
        // Arrange
        String barcode = "1234567890";
        String quantityStr = "invalid";
        int lineNumber = 2;

        InventoryUploadForm form = new InventoryUploadForm();
        form.setBarcode(barcode);
        form.setQuantity(quantityStr);

        ProductEntity product = createProduct(1L, "Test Product", barcode);
        when(productService.getByBarcode(barcode)).thenReturn(product);

        // Act - should throw ApiException
        flow.processInventoryForm(form, lineNumber);
    }

    @Test
    public void testProcessInventoryFormZeroQuantity() throws ApiException {
        // Arrange
        String barcode = "1234567890";
        String quantityStr = "0";
        int lineNumber = 2;

        InventoryUploadForm form = new InventoryUploadForm();
        form.setBarcode(barcode);
        form.setQuantity(quantityStr);

        ProductEntity product = createProduct(1L, "Test Product", barcode);
        when(productService.getByBarcode(barcode)).thenReturn(product);

        InventoryEntity updatedInventory = new InventoryEntity();
        updatedInventory.setId(1L);
        updatedInventory.setProductId(product.getId());
        updatedInventory.setQuantity(0L);
        
        when(inventoryService.updateInventory(product.getId(), 0L)).thenReturn(updatedInventory);

        // Act
        InventoryData result = flow.processInventoryForm(form, lineNumber);

        // Assert
        assertEquals(product.getId(), result.getProductId());
        assertEquals(Long.valueOf(0L), result.getQuantity());
        verify(inventoryService).updateInventory(product.getId(), 0L);
    }

    @Test(expected = ApiException.class)
    public void testProcessInventoryFormNegativeQuantity() throws ApiException {
        // Arrange
        String barcode = "1234567890";
        String quantityStr = "-50";
        int lineNumber = 2;

        InventoryUploadForm form = new InventoryUploadForm();
        form.setBarcode(barcode);
        form.setQuantity(quantityStr);

        ProductEntity product = createProduct(1L, "Test Product", barcode);
        when(productService.getByBarcode(barcode)).thenReturn(product);

        // Act - should throw ApiException because quantity is negative
        flow.processInventoryForm(form, lineNumber);
    }

    private ProductEntity createProduct(Long id, String name, String barcode) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        product.setMrp(new BigDecimal("99.99"));
        product.setClientId(1L);
        return product;
    }
}