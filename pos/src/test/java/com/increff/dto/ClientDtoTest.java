package com.increff.dto;

import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientData;
import com.increff.model.clients.ClientSearchForm;
import com.increff.service.ApiException;
import com.increff.spring.AbstractUnitTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;

import static org.junit.Assert.*;

@Transactional
public class ClientDtoTest extends AbstractUnitTest {

    @Autowired
    private ClientDto dto;



    @Test
    public void testAdd() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");

        // When
        ClientData result = dto.add(form);

        // Then
        assertNotNull(result);
        assertEquals("Test Client", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhoneNumber());
    }

    @Test
    public void testAddDuplicateEmail() throws ApiException {
        // Given
        ClientForm form1 = new ClientForm();
        form1.setName("Test Client 1");
        form1.setEmail("duplicate@example.com");
        form1.setPhoneNumber("1234567890");

        ClientForm form2 = new ClientForm();
        form2.setName("Test Client 2");
        form2.setEmail("duplicate@example.com");
        form2.setPhoneNumber("0987654321");

        // When/Then
        dto.add(form1); // Should succeed
        try {
            dto.add(form2); // Should throw ApiException
            fail("Expected ApiException was not thrown");
        } catch (ApiException e) {
            assertTrue(e.getMessage().contains("already exists"));
        }
    }

    @Test
    public void testGet() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");
        ClientData added = dto.add(form);

        // When
        ClientData result = dto.get(added.getId());

        // Then
        assertNotNull(result);
        assertEquals(added.getId(), result.getId());
        assertEquals("Test Client", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    public void testGetAll() throws ApiException {
        // Given
        ClientForm form1 = new ClientForm();
        form1.setName("Test Client 1");
        form1.setEmail("test1@example.com");
        form1.setPhoneNumber("1234567890");
        dto.add(form1);

        ClientForm form2 = new ClientForm();
        form2.setName("Test Client 2");
        form2.setEmail("test2@example.com");
        form2.setPhoneNumber("0987654321");
        dto.add(form2);

        // When
        List<ClientData> results = dto.getAll();

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void testUpdate() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");
        ClientData added = dto.add(form);

        ClientForm updateForm = new ClientForm();
        updateForm.setName("Updated Client");
        updateForm.setEmail("updated@example.com");
        updateForm.setPhoneNumber("9876543210");

        // When
        ClientData result = dto.update(added.getId(), updateForm);

        // Then
        assertNotNull(result);
        assertEquals(added.getId(), result.getId());
        assertEquals("Updated Client", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("9876543210", result.getPhoneNumber());
    }

    @Test
    public void testDelete() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");
        ClientData added = dto.add(form);

        // When
        dto.delete(added.getId());

        // Then
        try {
            dto.get(added.getId());
            fail("Expected ApiException was not thrown");
        } catch (ApiException e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test
    public void testSearch() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");
        dto.add(form);

        ClientSearchForm searchForm = new ClientSearchForm();
        searchForm.setName("Test");

        // When
        List<ClientData> results = dto.search(searchForm);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Client", results.get(0).getName());
    }
} 