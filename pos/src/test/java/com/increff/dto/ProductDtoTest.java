package com.increff.dto;

import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductSearchForm;
import com.increff.service.ApiException;
import com.increff.spring.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class ProductDtoTest extends AbstractUnitTest {

    @Autowired
    private ProductDto productDto;

    @Autowired
    private ClientDto clientDto;

    private Long clientId;

    @Before
    public void setUp() throws ApiException {
        // Create a test client to use in all tests
        ClientForm clientForm = new ClientForm();
        clientForm.setName("Test Client");
        clientForm.setEmail("test@example.com");
        clientForm.setPhoneNumber("1234567890");
        ClientData clientData = clientDto.add(clientForm);
        clientId = clientData.getId();
    }

    private ProductForm createProductForm(String name, String barcode, BigDecimal mrp) {
        ProductForm form = new ProductForm();
        form.setName(name);
        form.setBarcode(barcode);
        form.setMrp(mrp);
        form.setClientId(clientId);
        return form;
    }

    @Test
    public void testAdd() throws ApiException {
        ProductForm form = createProductForm(
            "Test Product",
            "1234567890",
            new BigDecimal("99.99")
        );

        ProductData result = productDto.add(form);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("1234567890", result.getBarcode());
        assertEquals(0, new BigDecimal("99.99").compareTo(result.getMrp()));
        assertEquals(clientId, result.getClientId());
    }

    @Test(expected = ApiException.class)
    public void testAddWithNullForm() throws ApiException {
        productDto.add(null);
    }

    @Test(expected = ApiException.class)
    public void testAddWithEmptyName() throws ApiException {
        ProductForm form = createProductForm(
            "",
            "1234567890",
            new BigDecimal("99.99")
        );
        productDto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testAddWithEmptyBarcode() throws ApiException {
        ProductForm form = createProductForm(
            "Test Product",
            "",
            new BigDecimal("99.99")
        );
        productDto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testAddWithLongBarcode() throws ApiException {
        StringBuilder longBarcode = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            longBarcode.append("1");
        }
        
        ProductForm form = createProductForm(
            "Test Product",
            longBarcode.toString(),
            new BigDecimal("99.99")
        );
        productDto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testAddWithNullClientId() throws ApiException {
        ProductForm form = createProductForm(
            "Test Product",
            "1234567890",
            new BigDecimal("99.99")
        );
        form.setClientId(null);
        productDto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testAddWithZeroMrp() throws ApiException {
        ProductForm form = createProductForm(
            "Test Product",
            "1234567890",
            BigDecimal.ZERO
        );
        productDto.add(form);
    }

    @Test
    public void testGetAll() throws ApiException {
        // Add two products
        productDto.add(createProductForm(
            "Product 1",
            "1234567890",
            new BigDecimal("99.99")
        ));
        productDto.add(createProductForm(
            "Product 2",
            "0987654321",
            new BigDecimal("199.99")
        ));

        List<ProductData> results = productDto.getAll(0, 10);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Product 1", results.get(0).getName());
        assertEquals("1234567890", results.get(0).getBarcode());
    }

    @Test
    public void testUpdate() throws ApiException {
        // First add a product
        ProductData added = productDto.add(createProductForm(
            "Original Product",
            "1234567890",
            new BigDecimal("99.99")
        ));

        // Now update it
        ProductForm updateForm = createProductForm(
            "Updated Product",
            "0987654321",
            new BigDecimal("199.99")
        );

        ProductData updated = productDto.update(added.getId(), updateForm);

        assertNotNull(updated);
        assertEquals("Updated Product", updated.getName());
        assertEquals("0987654321", updated.getBarcode());
        assertEquals(0, new BigDecimal("199.99").compareTo(updated.getMrp()));
    }

    @Test
    public void testDelete() throws ApiException {
        ProductData added = productDto.add(createProductForm(
            "Test Product",
            "1234567890",
            new BigDecimal("99.99")
        ));

        productDto.delete(added.getId());

        try {
            productDto.get(added.getId());
            fail("Expected ApiException was not thrown");
        } catch (ApiException e) {
            // Expected exception
        }
    }

    @Test
    public void testSearch() throws ApiException {
        productDto.add(createProductForm(
            "Test Product 1",
            "1234567890",
            new BigDecimal("99.99")
        ));
        productDto.add(createProductForm(
            "Test Product 2",
            "0987654321",
            new BigDecimal("199.99")
        ));

        ProductSearchForm searchForm = new ProductSearchForm();
        searchForm.setName("Test");

        List<ProductData> results = productDto.search(searchForm, 0, 10);

        assertNotNull(results);
        assertTrue(results.size() >= 2);
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("Test Product 1")));
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("Test Product 2")));
    }
} 