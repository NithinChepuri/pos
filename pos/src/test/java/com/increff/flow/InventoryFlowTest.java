package com.increff.flow;

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
        String barcode = "1234567890";
        String quantityStr = "100";
        int lineNumber = 2;
        
        InventoryUploadForm form = new InventoryUploadForm();
        form.setBarcode(barcode);
        form.setQuantity(quantityStr);
        
        ProductEntity product = createProduct(1L, "Test Product", barcode);
        when(productService.getByBarcode(barcode)).thenReturn(product);
        
        // Act
        InventoryData result = flow.processInventoryForm(form, lineNumber);
        
        // Assert
        assertEquals(product.getId(), result.getProductId());
        assertEquals(Long.valueOf(100L), result.getQuantity());
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
        
        // Act
        flow.processInventoryForm(form, lineNumber);
    }

    @Test(expected = NumberFormatException.class)
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
        
        // Act
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
        
        // Act
        InventoryData result = flow.processInventoryForm(form, lineNumber);
        
        // Assert
        assertEquals(product.getId(), result.getProductId());
        assertEquals(Long.valueOf(0L), result.getQuantity());
        verify(inventoryService).updateInventory(product.getId(), 0L);
    }

    @Test
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
        
        // Act
        InventoryData result = flow.processInventoryForm(form, lineNumber);
        
        // Assert
        assertEquals(product.getId(), result.getProductId());
        assertEquals(Long.valueOf(-50L), result.getQuantity());
        verify(inventoryService).updateInventory(product.getId(), -50L);
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