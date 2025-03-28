package com.increff.flow;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.service.ClientService;
import com.increff.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductFlowTest {

    @Mock
    private ProductService productService;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ProductFlow flow;

    @Test
    public void testAdd() throws ApiException {
        // Given
        ProductEntity product = createProduct("Test Product", "1234567890");
        when(productService.getByBarcode("1234567890")).thenReturn(null);
        when(clientService.exists(1L)).thenReturn(true);
        when(productService.addProduct(product)).thenReturn(product);

        // When
        ProductEntity result = flow.add(product);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productService).addProduct(product);
    }

    @Test(expected = ApiException.class)
    public void testAddDuplicateBarcode() throws ApiException {
        // Given
        ProductEntity product = createProduct("Test Product", "1234567890");
        when(productService.getByBarcode("1234567890")).thenReturn(createProduct("Existing Product", "1234567890"));

        // When/Then
        flow.add(product); // Should throw ApiException
    }

    @Test(expected = ApiException.class)
    public void testAddInvalidClient() throws ApiException {
        // Given
        ProductEntity product = createProduct("Test Product", "1234567890");
        when(productService.getByBarcode("1234567890")).thenReturn(null);
        when(clientService.exists(1L)).thenReturn(false);

        // When/Then
        flow.add(product); // Should throw ApiException
    }

    @Test
    public void testUpdate() throws ApiException {
        // Given
        Long id = 1L;
        ProductEntity existing = createProduct("Original", "1234567890");
        ProductEntity updated = createProduct("Updated", "0987654321");
        
        when(productService.get(id)).thenReturn(existing);
        when(clientService.exists(1L)).thenReturn(true);
        when(productService.updateProduct(existing, updated)).thenReturn(updated);

        // When
        ProductEntity result = flow.update(id, updated);

        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getName());
        assertEquals("0987654321", result.getBarcode());
    }

    @Test(expected = ApiException.class)
    public void testUpdateNonExistentProduct() throws ApiException {
        // Given
        Long id = 1L;
        ProductEntity updated = createProduct("Updated", "0987654321");
        when(productService.get(id)).thenReturn(null);

        // When/Then
        flow.update(id, updated); // Should throw ApiException
    }

    @Test(expected = ApiException.class)
    public void testUpdateWithDuplicateBarcode() throws ApiException {
        // Given
        Long id = 1L;
        ProductEntity existing = createProduct("Original", "1234567890");
        existing.setId(id);
        
        ProductEntity updated = createProduct("Updated", "0987654321");
        updated.setId(id);
        
        ProductEntity conflicting = createProduct("Conflicting", "0987654321");
        conflicting.setId(2L);
        
        when(productService.get(id)).thenReturn(existing);
        when(productService.getByBarcode("0987654321")).thenReturn(conflicting);

        // When/Then
        flow.update(id, updated); // Should throw ApiException
    }

    @Test
    public void testUploadProducts() {
        // Given
        List<ProductEntity> products = Arrays.asList(
            createProduct("Product 1", "1234567890"),
            createProduct("Product 2", "0987654321")
        );
        
        List<ProductForm> forms = Arrays.asList(
            createProductForm("Product 1", "1234567890"),
            createProductForm("Product 2", "0987654321")
        );

        when(productService.getByBarcode(anyString())).thenReturn(null);
        when(clientService.exists(anyLong())).thenReturn(true);
        when(productService.addProduct(any(ProductEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        UploadResult<ProductEntity> result = flow.uploadProducts(products, forms);

        // Then
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());
        assertEquals(2, result.getSuccessfulEntries().size());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    public void testUploadProductsWithErrors() {
        // Given
        List<ProductEntity> products = Arrays.asList(
            createProduct("Product 1", "1234567890"),
            createProduct("Product 2", "0987654321"),
            createProduct("Product 3", "1111111111")
        );
        
        List<ProductForm> forms = Arrays.asList(
            createProductForm("Product 1", "1234567890"),
            createProductForm("Product 2", "0987654321"),
            createProductForm("Product 3", "1111111111")
        );

        // First product succeeds
        when(productService.getByBarcode("1234567890")).thenReturn(null);
        
        // Second product fails - duplicate barcode
        when(productService.getByBarcode("0987654321")).thenReturn(createProduct("Existing", "0987654321"));
        
        // Third product fails - invalid client
        when(productService.getByBarcode("1111111111")).thenReturn(null);
        
        when(clientService.exists(1L)).thenReturn(true).thenReturn(false);
        when(productService.addProduct(any(ProductEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        UploadResult<ProductEntity> result = flow.uploadProducts(products, forms);

        // Then
        assertEquals(1, result.getSuccessCount());
        assertEquals(2, result.getErrorCount());
        assertEquals(1, result.getSuccessfulEntries().size());
        assertEquals(2, result.getErrors().size());
    }

    private ProductEntity createProduct(String name, String barcode) {
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setName(name);
        product.setBarcode(barcode);
        product.setMrp(new BigDecimal("99.99"));
        product.setClientId(1L);
        return product;
    }

    private ProductForm createProductForm(String name, String barcode) {
        ProductForm form = new ProductForm();
        form.setName(name);
        form.setBarcode(barcode);
        form.setMrp(new BigDecimal("99.99"));
        form.setClientId(1L);
        return form;
    }
} 