package com.increff.dto;

import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventorySearchForm;
import com.increff.model.inventory.InventoryUploadForm;
import com.increff.model.products.ProductForm;
import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientData;
import com.increff.service.ApiException;
import com.increff.spring.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class InventoryDtoTest extends AbstractUnitTest {

    @Autowired
    private InventoryDto inventoryDto;

    @Autowired
    private ProductDto productDto;

    @Autowired
    private ClientDto clientDto;

    private Long clientId;
    private Long productId;

    @Before
    public void setUp() throws ApiException {
        // Create a test client
        ClientForm clientForm = new ClientForm();
        clientForm.setName("Test Client");
        clientForm.setEmail("test@example.com");
        clientForm.setPhoneNumber("1234567890");
        ClientData clientData = clientDto.add(clientForm);
        clientId = clientData.getId();

        // Create a test product
        ProductForm productForm = new ProductForm();
        productForm.setName("Test Product");
        productForm.setBarcode("1234567890");
        productForm.setMrp(new BigDecimal("99.99"));
        productForm.setClientId(clientId);
        productId = productDto.add(productForm).getId();
    }

    @Test
    public void testAdd() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(100L);

        // When
        InventoryData result = inventoryDto.add(form);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(Long.valueOf(100L), result.getQuantity());
        assertEquals("Test Product", result.getProductName());
        assertEquals("1234567890", result.getBarcode());
    }

    @Test(expected = ApiException.class)
    public void testAddWithNegativeQuantity() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(-100L);

        // When - should throw ApiException
        inventoryDto.add(form);
    }

    @Test
    public void testGet() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(100L);
        InventoryData addedInventory = inventoryDto.add(form);

        // When
        InventoryData result = inventoryDto.get(addedInventory.getId());

        // Then
        assertNotNull(result);
        assertEquals(addedInventory.getId(), result.getId());
        assertEquals(productId, result.getProductId());
        assertEquals(Long.valueOf(100L), result.getQuantity());
        assertEquals("Test Product", result.getProductName());
        assertEquals("1234567890", result.getBarcode());
    }

    @Test(expected = ApiException.class)
    public void testGetInventoryNotFound() throws ApiException {
        // When - should throw ApiException
        inventoryDto.get(999L);
    }

    @Test
    public void testGetAll() throws ApiException {
        // Given
        InventoryForm form1 = new InventoryForm();
        form1.setProductId(productId);
        form1.setQuantity(100L);
        inventoryDto.add(form1);

        // Create another product and inventory
        ProductForm productForm2 = new ProductForm();
        productForm2.setName("Test Product 2");
        productForm2.setBarcode("0987654321");
        productForm2.setMrp(new BigDecimal("199.99"));
        productForm2.setClientId(clientId);
        Long productId2 = productDto.add(productForm2).getId();

        InventoryForm form2 = new InventoryForm();
        form2.setProductId(productId2);
        form2.setQuantity(200L);
        inventoryDto.add(form2);

        // When
        List<InventoryData> results = inventoryDto.getAll(0, 10);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // Verify first inventory
        InventoryData inventory1 = results.stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
        assertNotNull(inventory1);
        assertEquals(Long.valueOf(100L), inventory1.getQuantity());
        assertEquals("Test Product", inventory1.getProductName());
        assertEquals("1234567890", inventory1.getBarcode());
        
        // Verify second inventory
        InventoryData inventory2 = results.stream()
            .filter(i -> i.getProductId().equals(productId2))
            .findFirst()
            .orElse(null);
        assertNotNull(inventory2);
        assertEquals(Long.valueOf(200L), inventory2.getQuantity());
        assertEquals("Test Product 2", inventory2.getProductName());
        assertEquals("0987654321", inventory2.getBarcode());
    }

    @Test
    public void testUpdate() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(100L);
        InventoryData addedInventory = inventoryDto.add(form);

        InventoryForm updateForm = new InventoryForm();
        updateForm.setProductId(productId);
        updateForm.setQuantity(200L);

        // When
        inventoryDto.update(addedInventory.getId(), updateForm);

        // Then
        InventoryData updatedInventory = inventoryDto.get(addedInventory.getId());
        assertEquals(Long.valueOf(200L), updatedInventory.getQuantity());
    }

    @Test
    public void testSearch() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(100L);
        inventoryDto.add(form);

        InventorySearchForm searchForm = new InventorySearchForm();
        searchForm.setBarcode("1234");

        // When
        List<InventoryData> results = inventoryDto.search(searchForm, 0, 10);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(productId, results.get(0).getProductId());
        assertEquals("Test Product", results.get(0).getProductName());
    }

    @Test
    public void testProcessUpload() throws IOException, ApiException {
        // Given
        String tsvContent = "Barcode\tQuantity\n1234567890\t150";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.tsv",
            "text/tab-separated-values", 
            tsvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        ResponseEntity<com.increff.model.inventory.UploadResponse> response = inventoryDto.processUpload(file);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalRows());
        assertEquals(1, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());

        // Verify inventory was updated
        InventoryData inventory = inventoryDto.getAll(0, 10)
            .stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
        assertNotNull(inventory);
        assertEquals(Long.valueOf(150L), inventory.getQuantity());
    }

    @Test
    public void testProcessUploadWithErrors() throws IOException {
        // Given
        String tsvContent = "Barcode\tQuantity\nnonexistent\t150";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.tsv",
            "text/tab-separated-values", 
            tsvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        ResponseEntity<com.increff.model.inventory.UploadResponse> response = inventoryDto.processUpload(file);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalRows());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(1, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertTrue(response.getBody().getErrors().get(0).getMessage().contains("not found"));
    }

    @Test
    public void testProcessUploadWithInvalidFormat() throws IOException {
        // Given
        String tsvContent = "Barcode\tQuantity\n1234567890\tinvalid";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.tsv",
            "text/tab-separated-values", 
            tsvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        ResponseEntity<com.increff.model.inventory.UploadResponse> response = inventoryDto.processUpload(file);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalRows());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(1, response.getBody().getErrorCount());
    }

    @Test
    public void testProcessUploadWithInvalidHeader() throws IOException {
        // Given
        String tsvContent = "InvalidHeader1\tInvalidHeader2\n1234567890\t150";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.tsv",
            "text/tab-separated-values", 
            tsvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        ResponseEntity<com.increff.model.inventory.UploadResponse> response = inventoryDto.processUpload(file);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalRows());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertTrue(response.getBody().getErrors().get(0).getMessage().contains("Invalid header"));
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
        ResponseEntity<com.increff.model.inventory.UploadResponse> response = inventoryDto.processUpload(file);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalRows());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertTrue(response.getBody().getErrors().get(0).getMessage().contains("empty"));
    }
} 