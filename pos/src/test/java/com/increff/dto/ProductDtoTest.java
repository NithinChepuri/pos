package com.increff.dto;

import com.increff.entity.ProductEntity;
import com.increff.flow.ProductFlow;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.ProductUpdateForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.service.ProductService;
import com.increff.util.ConversionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductDtoTest {

    @Mock
    private ProductService service;
    
    @Mock
    private ProductFlow flow;

    @InjectMocks
    private ProductDto dto;

    private Validator validator;

    public ProductDtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testAdd() throws ApiException {
        // Given
        ProductForm form = createProductForm("Test Product", "1234567890", new BigDecimal("99.99"));
        
        ProductEntity entity = new ProductEntity();
        entity.setId(1L);
        entity.setName("Test Product");
        entity.setBarcode("1234567890");
        entity.setMrp(new BigDecimal("99.99"));
        entity.setClientId(1L);
        
        when(flow.add(any(ProductEntity.class))).thenReturn(entity);

        // When
        ProductData result = dto.add(form);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("1234567890", result.getBarcode());
        assertEquals(0, new BigDecimal("99.99").compareTo(result.getMrp()));
    }

    @Test
    public void testGet() throws ApiException {
        // Given
        Long id = 1L;
        ProductEntity entity = new ProductEntity();
        entity.setId(id);
        entity.setName("Test Product");
        entity.setBarcode("1234567890");
        entity.setMrp(new BigDecimal("99.99"));
        entity.setClientId(1L);
        
        when(service.getChecked(id)).thenReturn(entity);

        // When
        ProductData result = dto.get(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("1234567890", result.getBarcode());
    }

    @Test
    public void testGetAll() {
        // Given
        List<ProductEntity> entities = new ArrayList<>();
        
        ProductEntity entity1 = new ProductEntity();
        entity1.setId(1L);
        entity1.setName("Product 1");
        entity1.setBarcode("1234567890");
        entity1.setMrp(new BigDecimal("99.99"));
        entity1.setClientId(1L);
        entities.add(entity1);
        
        ProductEntity entity2 = new ProductEntity();
        entity2.setId(2L);
        entity2.setName("Product 2");
        entity2.setBarcode("0987654321");
        entity2.setMrp(new BigDecimal("199.99"));
        entity2.setClientId(1L);
        entities.add(entity2);
        
        when(service.getAll(0, 10)).thenReturn(entities);

        // When
        List<ProductData> results = dto.getAll(0, 10);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Product 1", results.get(0).getName());
        assertEquals("Product 2", results.get(1).getName());
    }

    @Test
    public void testUpdate() throws ApiException {
        // Given
        Long id = 1L;
        ProductUpdateForm form = new ProductUpdateForm();
        form.setName("Updated Product");
        form.setBarcode("0987654321");
        form.setMrp(new BigDecimal("199.99"));
        form.setClientId(1L);
        
        ProductEntity updatedEntity = new ProductEntity();
        updatedEntity.setId(id);
        updatedEntity.setName("Updated Product");
        updatedEntity.setBarcode("0987654321");
        updatedEntity.setMrp(new BigDecimal("199.99"));
        updatedEntity.setClientId(1L);
        
        when(flow.update(eq(id), any(ProductEntity.class))).thenReturn(updatedEntity);
        
        // When
        ProductData result = dto.update(id, form);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Updated Product", result.getName());
        assertEquals("0987654321", result.getBarcode());
        assertEquals(0, new BigDecimal("199.99").compareTo(result.getMrp()));
    }

    @Test
    public void testDelete() throws ApiException {
        // Given
        Long id = 1L;
        doNothing().when(service).deleteProduct(id);
        
        // When
        dto.delete(id);
        
        // Then
        verify(service).deleteProduct(id);
    }
    
    @Test
    public void testSearch() {
        // Given
        ProductSearchForm searchForm = new ProductSearchForm();
        searchForm.setName("Test");
        
        List<ProductEntity> entities = new ArrayList<>();
        ProductEntity entity = new ProductEntity();
        entity.setId(1L);
        entity.setName("Test Product");
        entity.setBarcode("1234567890");
        entity.setMrp(new BigDecimal("99.99"));
        entity.setClientId(1L);
        entities.add(entity);
        
        when(service.search(any(ProductSearchForm.class), eq(0), eq(10))).thenReturn(entities);
        
        // When
        List<ProductData> results = dto.search(searchForm, 0, 10);
        
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
        form.setClientId(1L);

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
        form.setClientId(1L);
        return form;
    }
} 