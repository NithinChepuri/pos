package com.increff.dto;

import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventorySearchForm;
import com.increff.model.inventory.InventoryUpdateForm;
import com.increff.model.products.ProductForm;
import com.increff.model.clients.ClientForm;
import com.increff.service.ApiException;
import com.increff.spring.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

@Transactional
public class InventoryDtoTest extends AbstractUnitTest {

    @Autowired
    private InventoryDto inventoryDto;

    @Autowired
    private ProductDto productDto;

    @Autowired
    private ClientDto clientDto;

    private Long productId;
    private String barcode;

    @Before
    public void setUp() throws ApiException {
        // Create client
        ClientForm clientForm = new ClientForm();
        clientForm.setName("Test Client");
        clientForm.setEmail("test@example.com");
        clientForm.setPhoneNumber("1234567890");
        Long clientId = clientDto.add(clientForm).getId();

        // Create product
        ProductForm productForm = new ProductForm();
        productForm.setName("Test Product");
        productForm.setBarcode("1234567890");
        productForm.setMrp(new BigDecimal("99.99"));
        productForm.setClientId(clientId);
        productId = productDto.add(productForm).getId();
        barcode = productForm.getBarcode();
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
    }

    @Test
    public void testGet() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(100L);
        InventoryData added = inventoryDto.add(form);

        // When
        InventoryData result = inventoryDto.get(added.getId());

        // Then
        assertNotNull(result);
        assertEquals(added.getId(), result.getId());
        assertEquals(Long.valueOf(100L), result.getQuantity());
    }

    @Test
    public void testGetAll() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(100L);
        inventoryDto.add(form);

        // When
        List<InventoryData> results = inventoryDto.getAll(0, 10);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    public void testUpdate() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(100L);
        InventoryData added = inventoryDto.add(form);

        InventoryUpdateForm updateForm = new InventoryUpdateForm();
        updateForm.setQuantity(200L);

        // When
        inventoryDto.update(added.getId(), updateForm);
        InventoryData updated = inventoryDto.get(added.getId());

        // Then
        assertEquals(Long.valueOf(200L), updated.getQuantity());
    }

    @Test
    public void testSearch() throws ApiException {
        // Given
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(100L);
        inventoryDto.add(form);

        InventorySearchForm searchForm = new InventorySearchForm();
        searchForm.setBarcode(barcode);

        // When
        List<InventoryData> results = inventoryDto.search(searchForm, 0, 10);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void testProcessUpload() throws Exception {
        // Given
        String tsvContent = "barcode\tquantity\n1234567890\t100";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.tsv",
            "text/tab-separated-values",
            tsvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        ResponseEntity<?> response = inventoryDto.processUpload(file);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
} 