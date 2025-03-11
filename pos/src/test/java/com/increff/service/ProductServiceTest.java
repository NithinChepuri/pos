package com.increff.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.increff.dao.ProductDao;
import com.increff.entity.ProductEntity;
import com.increff.model.ProductForm;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;

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

    private ProductEntity createProduct(String name, String barcode, BigDecimal mrp, Long clientId) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setBarcode(barcode);
        product.setMrp(mrp);
        product.setClientId(clientId);
        return product;
    }
} 