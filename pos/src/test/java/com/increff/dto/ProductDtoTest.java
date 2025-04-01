package com.increff.dto;

import com.increff.entity.ProductEntity;
import com.increff.flow.ProductFlow;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.ProductUpdateForm;
import com.increff.model.products.UploadResult;
import com.increff.model.clients.ClientForm;
import com.increff.service.ApiException;
import com.increff.service.ProductService;
import com.increff.util.ConversionUtil;
import com.increff.spring.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@Transactional
public class ProductDtoTest extends AbstractUnitTest {

    @Autowired
    private ProductDto productDto;

    @Autowired
    private ClientDto clientDto;

    private Long clientId;

    private Validator validator;

    public ProductDtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Before
    public void setUp() throws ApiException {
        ClientForm clientForm = new ClientForm();
        clientForm.setName("Test Client");
        clientForm.setEmail("test@example.com");
        clientForm.setPhoneNumber("1234567890");
        clientId = clientDto.add(clientForm).getId();
    }

    @Test
    public void testAdd() throws ApiException {
        // Given
        ProductForm form = createProductForm("Test Product", "1234567890", new BigDecimal("99.99"));

        // When
        ProductData result = productDto.add(form);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("1234567890", result.getBarcode());
        assertEquals(0, new BigDecimal("99.99").compareTo(result.getMrp()));
    }

    @Test
    public void testGet() throws ApiException {
        // Given
        ProductForm form = createProductForm("Test Product", "1234567890", new BigDecimal("99.99"));
        ProductData added = productDto.add(form);

        // When
        ProductData result = productDto.get(added.getId());

        // Then
        assertNotNull(result);
        assertEquals(added.getId(), result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    public void testGetAll() throws ApiException {
        // Given
        productDto.add(createProductForm("Product 1", "1234567890", new BigDecimal("99.99")));
        productDto.add(createProductForm("Product 2", "0987654321", new BigDecimal("199.99")));

        // When
        List<ProductData> results = productDto.getAll(0, 10);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void testUpdate() throws ApiException {
        // Given
        ProductData added = productDto.add(createProductForm("Test Product", "1234567890", new BigDecimal("99.99")));
        
        ProductUpdateForm updateForm = new ProductUpdateForm();
        updateForm.setName("Updated Product");
        updateForm.setBarcode("0987654321");
        updateForm.setMrp(new BigDecimal("199.99"));
        updateForm.setClientId(clientId);

        // When
        ProductData result = productDto.update(added.getId(), updateForm);

        // Then
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals("0987654321", result.getBarcode());
    }

    @Test
    public void testSearch() throws ApiException {
        // Given
        productDto.add(createProductForm("Test Product", "1234567890", new BigDecimal("99.99")));
        
        ProductSearchForm searchForm = new ProductSearchForm();
        searchForm.setName("Test");

        // When
        List<ProductData> results = productDto.search(searchForm, 0, 10);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Product", results.get(0).getName());
    }

    @Test
    public void testInvalidProductForm() {
        // Given
        ProductForm form = new ProductForm();
        form.setName(""); // Empty name
        form.setBarcode("1234567890");
        form.setMrp(new BigDecimal("99.99"));
        form.setClientId(clientId);

        // When
        Set<ConstraintViolation<ProductForm>> violations = validator.validate(form);

        // Then
        assertFalse(violations.isEmpty());
        boolean hasNameViolation = false;
        for (ConstraintViolation<ProductForm> violation : violations) {
            if (violation.getPropertyPath().toString().equals("name")) {
                hasNameViolation = true;
                break;
            }
        }
        assertTrue("Should have name validation violation", hasNameViolation);
    }

    private ProductForm createProductForm(String name, String barcode, BigDecimal mrp) {
        ProductForm form = new ProductForm();
        form.setName(name);
        form.setBarcode(barcode);
        form.setMrp(mrp);
        form.setClientId(clientId);
        return form;
    }
} 