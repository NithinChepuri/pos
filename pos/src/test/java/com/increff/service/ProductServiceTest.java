package com.increff.service;

import com.increff.dao.ProductDao;
import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductSearchForm;
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
public class ProductServiceTest {

    @Mock
    private ProductDao dao;

    @InjectMocks
    private ProductService service;

    @Test
    public void testAdd() throws ApiException {
        ProductEntity product = createProduct(null, "Test Product", "TESTSKU", new BigDecimal("100.00"));
        when(dao.selectByBarcode(anyString())).thenReturn(null);
        
        service.add(product);
        
        verify(dao).insert(product);
    }

    @Test(expected = ApiException.class)
    public void testAddDuplicateBarcode() throws ApiException {
        ProductEntity existingProduct = createProduct(1L, "Existing Product", "TESTSKU", new BigDecimal("100.00"));
        ProductEntity newProduct = createProduct(null, "New Product", "TESTSKU", new BigDecimal("200.00"));
        
        when(dao.selectByBarcode("TESTSKU")).thenReturn(existingProduct);
        doThrow(new ApiException("Product with barcode TESTSKU already exists"))
            .when(dao).insert(any(ProductEntity.class));
        
        service.add(newProduct);
    }

    @Test
    public void testGet() throws ApiException {
        Long id = 1L;
        ProductEntity product = createProduct(id, "Test Product", "TESTSKU", new BigDecimal("100.00"));
        when(dao.select(id)).thenReturn(product);
        
        ProductEntity result = service.get(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test(expected = ApiException.class)
    public void testGetNonExistent() throws ApiException {
        Long id = 1L;
        when(dao.select(id)).thenReturn(null);
        doThrow(new ApiException("Product with id " + id + " not found"))
            .when(dao).select(id);
        
        service.get(id);
    }

    @Test
    public void testGetAll() {
        List<ProductEntity> products = Arrays.asList(
            createProduct(1L, "Product 1", "SKU1", new BigDecimal("100.00")),
            createProduct(2L, "Product 2", "SKU2", new BigDecimal("200.00"))
        );
        when(dao.selectAll(0, 10)).thenReturn(products);
        
        List<ProductEntity> results = service.getAll(0, 10);
        
        assertEquals(2, results.size());
    }

    @Test
    public void testUpdate() throws ApiException {
        Long id = 1L;
        ProductEntity existingProduct = createProduct(id, "Old Name", "SKU1", new BigDecimal("100.00"));
        ProductEntity updatedProduct = createProduct(id, "New Name", "SKU1", new BigDecimal("200.00"));
        
        when(dao.select(id)).thenReturn(existingProduct);
        when(dao.selectByBarcode(anyString())).thenReturn(null);
        
        service.update(updatedProduct);
        
        verify(dao).update(updatedProduct);
    }

    @Test
    public void testDelete() throws ApiException {
        Long id = 1L;
        ProductEntity product = createProduct(id, "Test Product", "TESTSKU", new BigDecimal("100.00"));
        when(dao.select(id)).thenReturn(product);
        
        service.delete(id);
        
        verify(dao).delete(product);
    }

    @Test
    public void testSearch() {
        ProductSearchForm form = new ProductSearchForm();
        form.setName("Test");
        
        List<ProductEntity> expectedResults = Arrays.asList(
            createProduct(1L, "Test Product 1", "SKU1", new BigDecimal("100.00")),
            createProduct(2L, "Test Product 2", "SKU2", new BigDecimal("200.00"))
        );
        when(dao.search(eq(form), eq(0), eq(10))).thenReturn(expectedResults);
        
        List<ProductEntity> results = service.search(form, 0, 10);
        
        assertEquals(2, results.size());
    }

    private ProductEntity createProduct(Long id, String name, String barcode, BigDecimal mrp) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        product.setMrp(mrp);
        return product;
    }
} 