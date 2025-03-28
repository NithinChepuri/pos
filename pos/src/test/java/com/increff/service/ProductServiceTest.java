package com.increff.service;

import com.increff.dao.ProductDao;
import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductSearchForm;
import org.junit.Before;
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

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductService service;

    @Before
    public void setUp() {
        when(orderItemService.existsByProductId(anyLong())).thenReturn(false);
        when(inventoryService.existsByProductId(anyLong())).thenReturn(false);
    }

    @Test
    public void testAdd() {
        // Given
        ProductEntity product = createProduct("Test Product", "1234567890");
        
        // When
        service.add(product);
        
        // Then
        verify(dao).insert(product);
    }

    @Test
    public void testAddProduct() {
        // Given
        ProductEntity product = createProduct("Test Product", "1234567890");
        
        // When
        ProductEntity result = service.addProduct(product);
        
        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(dao).insert(product);
    }

    @Test
    public void testGet() {
        // Given
        Long id = 1L;
        ProductEntity product = createProduct("Test Product", "1234567890");
        when(dao.select(id)).thenReturn(product);

        // When
        ProductEntity result = service.get(id);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
    }

    @Test
    public void testGetChecked() throws ApiException {
        // Given
        Long id = 1L;
        ProductEntity product = createProduct("Test Product", "1234567890");
        when(dao.select(id)).thenReturn(product);

        // When
        ProductEntity result = service.getChecked(id);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
    }

    @Test(expected = ApiException.class)
    public void testGetCheckedNotFound() throws ApiException {
        // Given
        Long id = 1L;
        when(dao.select(id)).thenReturn(null);

        // When/Then
        service.getChecked(id); // Should throw ApiException
    }

    @Test
    public void testGetAll() {
        // Given
        List<ProductEntity> products = Arrays.asList(
            createProduct("Product 1", "1234567890"),
            createProduct("Product 2", "0987654321")
        );
        when(dao.selectAll(0, 10)).thenReturn(products);

        // When
        List<ProductEntity> results = service.getAll(0, 10);

        // Then
        assertEquals(2, results.size());
    }

    @Test
    public void testUpdateProduct() {
        // Given
        ProductEntity existing = createProduct("Original", "1234567890");
        ProductEntity updated = createProduct("Updated", "0987654321");

        // When
        ProductEntity result = service.updateProduct(existing, updated);

        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getName());
        assertEquals("0987654321", result.getBarcode());
    }

    @Test
    public void testGetByBarcode() {
        // Given
        String barcode = "1234567890";
        ProductEntity product = createProduct("Test Product", barcode);
        when(dao.selectByBarcode(barcode)).thenReturn(product);

        // When
        ProductEntity result = service.getByBarcode(barcode);

        // Then
        assertNotNull(result);
        assertEquals(barcode, result.getBarcode());
    }

    @Test
    public void testSearch() {
        // Given
        ProductSearchForm form = new ProductSearchForm();
        List<ProductEntity> products = Arrays.asList(
            createProduct("Product 1", "1234567890"),
            createProduct("Product 2", "0987654321")
        );
        when(dao.search(form, 0, 10)).thenReturn(products);

        // When
        List<ProductEntity> results = service.search(form, 0, 10);

        // Then
        assertEquals(2, results.size());
    }

    @Test
    public void testDeleteProduct() throws ApiException {
        // Given
        Long id = 1L;
        ProductEntity product = createProduct("Test Product", "1234567890");
        when(dao.select(id)).thenReturn(product);

        // When
        service.deleteProduct(id);

        // Then
        verify(dao).delete(product);
    }

    @Test(expected = ApiException.class)
    public void testDeleteProductNotFound() throws ApiException {
        // Given
        Long id = 1L;
        when(dao.select(id)).thenReturn(null);

        // When/Then
        service.deleteProduct(id); // Should throw ApiException
    }

    @Test(expected = ApiException.class)
    public void testDeleteProductWithInventory() throws ApiException {
        // Given
        Long id = 1L;
        ProductEntity product = createProduct("Test Product", "1234567890");
        when(dao.select(id)).thenReturn(product);
        when(inventoryService.existsByProductId(id)).thenReturn(true);

        // When/Then
        service.deleteProduct(id); // Should throw ApiException
    }

    @Test(expected = ApiException.class)
    public void testDeleteProductWithOrders() throws ApiException {
        // Given
        Long id = 1L;
        ProductEntity product = createProduct("Test Product", "1234567890");
        when(dao.select(id)).thenReturn(product);
        when(orderItemService.existsByProductId(id)).thenReturn(true);

        // When/Then
        service.deleteProduct(id); // Should throw ApiException
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
} 