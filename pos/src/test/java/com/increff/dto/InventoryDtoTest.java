package com.increff.dto;

import com.increff.entity.InventoryEntity;
import com.increff.entity.ProductEntity;
import com.increff.flow.InventoryFlow;
import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventorySearchForm;
import com.increff.model.inventory.InventoryUpdateForm;
import com.increff.model.inventory.InventoryUploadForm;
import com.increff.model.inventory.UploadResponse;
import com.increff.service.ApiException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class InventoryDtoTest {

    @Mock
    private InventoryFlow inventoryFlow;

    @InjectMocks
    private InventoryDto dto;

    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testAdd() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(1L);
        form.setQuantity(100L);

        InventoryData expectedData = new InventoryData();
        expectedData.setId(1L);
        expectedData.setProductId(1L);
        expectedData.setQuantity(100L);
        expectedData.setProductName("Test Product");
        expectedData.setBarcode("1234567890");

        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(1L);
        entity.setQuantity(100L);

        when(inventoryFlow.add(any(InventoryEntity.class))).thenReturn(expectedData);

        // When
        InventoryData result = dto.add(form);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals(Long.valueOf(100L), result.getQuantity());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryFlow).add(any(InventoryEntity.class));
    }

    @Test
    public void testGet() throws ApiException {
        // Given
        Long id = 1L;
        InventoryEntity entity = new InventoryEntity();
        entity.setId(id);
        entity.setProductId(1L);
        entity.setQuantity(100L);

        when(inventoryFlow.get(id)).thenReturn(entity);
        when(inventoryFlow.convertEntityToData(entity)).thenReturn(createInventoryData());

        // When
        InventoryData result = dto.get(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(Long.valueOf(100L), result.getQuantity());
    }

    @Test
    public void testGetAll() {
        // Given
        List<InventoryData> expectedData = Arrays.asList(
            createInventoryData(),
            createInventoryData()
        );
        when(inventoryFlow.getAll(0, 10)).thenReturn(expectedData);

        // When
        List<InventoryData> results = dto.getAll(0, 10);

        // Then
        assertEquals(2, results.size());
        verify(inventoryFlow).getAll(0, 10);
    }

    @Test
    public void testUpdate() throws ApiException {
        // Given
        Long id = 1L;
        InventoryUpdateForm form = new InventoryUpdateForm();
        form.setQuantity(200L);

        // When
        dto.update(id, form);

        // Then
        verify(inventoryFlow).update(id, 200L);
    }

    @Test
    public void testSearch() {
        // Given
        InventorySearchForm form = new InventorySearchForm();
        form.setBarcode("1234");
        form.setProductName("Test");

        List<InventoryData> expectedData = Arrays.asList(
            createInventoryData(),
            createInventoryData()
        );
        when(inventoryFlow.search(eq(form), anyInt(), anyInt())).thenReturn(expectedData);

        // When
        List<InventoryData> results = dto.search(form, 0, 10);

        // Then
        assertEquals(2, results.size());
        verify(inventoryFlow).search(form, 0, 10);
    }

    @Test
    public void testProcessUpload() throws IOException {
        // Given
        String tsvContent = "barcode\tquantity\n1234567890\t100";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.tsv",
            "text/tab-separated-values",
            tsvContent.getBytes(StandardCharsets.UTF_8)
        );

        InventoryData successData = createInventoryData();
        when(inventoryFlow.processInventoryForm(any(InventoryUploadForm.class), anyInt()))
            .thenReturn(successData);

        // When
        ResponseEntity<UploadResponse> response = dto.processUpload(file);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalRows());
        assertEquals(1, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
    }

    @Test
    public void testProcessUploadWithErrors() throws IOException {
        // Given
        String tsvContent = "barcode\tquantity\n1234567890\tinvalid";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.tsv",
            "text/tab-separated-values",
            tsvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(inventoryFlow.processInventoryForm(any(InventoryUploadForm.class), anyInt()))
            .thenThrow(new ApiException("Invalid quantity format"));

        // When
        ResponseEntity<UploadResponse> response = dto.processUpload(file);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalRows());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(1, response.getBody().getErrorCount());
    }

    @Test
    public void testProcessUploadWithEmptyFile() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.tsv",
            "text/tab-separated-values",
            new byte[0]
        );

        // When
        ResponseEntity<UploadResponse> response = dto.processUpload(file);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalRows());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
    }

    private InventoryData createInventoryData() {
        InventoryData data = new InventoryData();
        data.setId(1L);
        data.setProductId(1L);
        data.setQuantity(100L);
        data.setProductName("Test Product");
        data.setBarcode("1234567890");
        return data;
    }

    private ProductEntity createProduct() {
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setName("Test Product");
        product.setBarcode("1234567890");
        product.setMrp(new BigDecimal("99.99"));
        product.setClientId(1L);
        return product;
    }
} 