package com.increff.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.increff.entity.ClientEntity;
import com.increff.entity.ProductEntity;
import com.increff.service.ProductService;
import com.increff.service.ClientService;
import com.increff.spring.QaConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = QaConfig.class)
@WebAppConfiguration
@Transactional
public class ProductServiceTest {

    @Autowired
    private ProductService service;

    @Autowired
    private ClientService clientService;

    private Long clientId;

    @Before
    public void init() {
        // Create a test client first
        ClientEntity client = new ClientEntity();
        client.setName("Test Client");
        client.setEmail("test@example.com");
        clientService.add(client);
        clientId = client.getId();
    }

    @Test
    public void testAdd() {
        ProductEntity product = new ProductEntity();
        product.setName("Test Product");
        product.setBarcode("12345");
        product.setClientId(clientId);  // Use the created client's ID
        product.setMrp(new BigDecimal("99.99"));
        
        service.add(product);
        
        assertNotNull(product.getId());
    }

    @Test
    public void testGet() {
        ProductEntity product = new ProductEntity();
        product.setName("Test Product");
        product.setBarcode("12345");
        product.setClientId(clientId);  // Use the created client's ID
        product.setMrp(new BigDecimal("99.99"));
        service.add(product);
        
        ProductEntity retrieved = service.get(product.getId());
        
        assertNotNull(retrieved);
        assertEquals(product.getName(), retrieved.getName());
        assertEquals(product.getBarcode(), retrieved.getBarcode());
    }

    @Test(expected = ApiException.class)
    public void testDuplicateBarcode() {
        ProductEntity product1 = new ProductEntity();
        product1.setName("Product 1");
        product1.setBarcode("same");
        product1.setClientId(clientId);  // Use the created client's ID
        product1.setMrp(new BigDecimal("99.99"));
        service.add(product1);

        ProductEntity product2 = new ProductEntity();
        product2.setName("Product 2");
        product2.setBarcode("same");
        product2.setClientId(clientId);  // Use the created client's ID
        product2.setMrp(new BigDecimal("199.99"));
        service.add(product2); // Should throw ApiException
    }

    @Test(expected = ApiException.class)
    public void testInvalidClientId() {
        ProductEntity product = new ProductEntity();
        product.setName("Test Product");
        product.setBarcode("12345");
        product.setClientId(999L);  // Non-existent client ID
        product.setMrp(new BigDecimal("99.99"));
        service.add(product); // Should throw ApiException
    }
} 