package com.increff.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.increff.dao.ProductDao;
import com.increff.entity.ProductEntity;
import com.increff.model.ProductForm;
import com.increff.model.ProductSearchForm;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.List;

public class ProductServiceTest extends AbstractUnitTest {

    @Autowired
    private ProductDao dao;

    @Autowired
    private ProductService service;

    @Test
    public void testAdd() {
        ProductEntity product = createProduct("test", "123", BigDecimal.TEN, 1L);
        service.add(product);
        ProductEntity fetched = service.get(product.getId());
        assertNotNull(fetched);
        assertEquals(product.getBarcode(), fetched.getBarcode());
    }

    @Test(expected = PersistenceException.class)
    public void testDuplicateBarcode() {
        ProductEntity product1 = createProduct("test1", "123", BigDecimal.TEN, 1L);
        ProductEntity product2 = createProduct("test2", "123", BigDecimal.TEN, 1L);
        service.add(product1);
        service.add(product2); // Should throw PersistenceException due to unique constraint
    }

    @Test
    public void testUpdate() {
        ProductEntity product = createProduct("test", "123", BigDecimal.TEN, 1L);
        service.add(product);
        product.setName("updated");
        service.update(product);
        ProductEntity fetched = service.get(product.getId());
        assertEquals("updated", fetched.getName());
    }

    @Test
    public void testGetByBarcode() {
        ProductEntity product = createProduct("test", "123", BigDecimal.TEN, 1L);
        service.add(product);
        ProductEntity fetched = service.getByBarcode("123");
        assertNotNull(fetched);
        assertEquals(product.getBarcode(), fetched.getBarcode());
    }

    @Test
    public void testGetAll() {
        // Add multiple products
        ProductEntity product1 = createProduct("test1", "123", BigDecimal.TEN, 1L);
        ProductEntity product2 = createProduct("test2", "456", BigDecimal.ONE, 1L);
        service.add(product1);
        service.add(product2);
        
        // Get all products with pagination
        List<ProductEntity> products = service.getAll(0, 10);
        
        // Verify results
        assertNotNull(products);
        assertEquals(2, products.size());
    }
    
    @Test
    public void testSearch() {
        // Add products with different attributes
        ProductEntity product1 = createProduct("apple", "123", BigDecimal.TEN, 1L);
        ProductEntity product2 = createProduct("banana", "456", BigDecimal.ONE, 1L);
        ProductEntity product3 = createProduct("apple juice", "789", BigDecimal.valueOf(5), 2L);
        service.add(product1);
        service.add(product2);
        service.add(product3);
        
        // Create search form to find products with "apple" in name
        ProductSearchForm form = new ProductSearchForm();
        form.setName("apple");
        
        // Search with pagination
        List<ProductEntity> results = service.search(form, 0, 10);
        
        // Verify results
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("apple")));
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("apple juice")));
    }
    
    @Test
    public void testSearchByClient() {
        // Add products with different client IDs
        ProductEntity product1 = createProduct("test1", "123", BigDecimal.TEN, 1L);
        ProductEntity product2 = createProduct("test2", "456", BigDecimal.ONE, 2L);
        service.add(product1);
        service.add(product2);
        
        // Create search form to find products for client ID 1
        ProductSearchForm form = new ProductSearchForm();
        form.setClientId(1L);
        
        // Search with pagination
        List<ProductEntity> results = service.search(form, 0, 10);
        
        // Verify results
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(Long.valueOf(1L), results.get(0).getClientId());
    }
    
    @Test
    public void testDelete() {
        // Add a product
        ProductEntity product = createProduct("test", "123", BigDecimal.TEN, 1L);
        service.add(product);
        
        // Verify product exists
        ProductEntity beforeDelete = service.get(product.getId());
        assertNotNull(beforeDelete);
        
        // Delete the product
        service.delete(product.getId());
        
        // Try to get the deleted product
        ProductEntity afterDelete = dao.select(product.getId());
        assertNull(afterDelete);
    }
    
    @Test
    public void testDeleteNonExistentProduct() {
        // Delete a product with a non-existent ID
        // Should not throw an exception
        service.delete(999L);
    }

    private ProductEntity createProduct(String name, String barcode, BigDecimal mrp, Long clientId) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setBarcode(barcode);
        product.setMrp(mrp);
        product.setClientId(clientId);
        return product;
    }
} 