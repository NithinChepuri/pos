package com.increff.dto;

import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientData;
import com.increff.model.clients.ClientSearchForm;
import com.increff.service.ApiException;
import com.increff.service.ClientService;
import com.increff.entity.ClientEntity;
import com.increff.util.ConversionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientDtoTest {

    @Mock
    private ClientService service;

    @InjectMocks
    private ClientDto dto;

    private Validator validator;

    public ClientDtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testAdd() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");

        ClientEntity entity = new ClientEntity();
        entity.setId(1L);
        entity.setName("Test Client");
        entity.setEmail("test@example.com");
        entity.setPhoneNumber("1234567890");

        when(service.add(any(ClientEntity.class))).thenReturn(entity);

        // When
        ClientData result = dto.add(form);

        // Then
        assertNotNull(result);
        assertEquals("Test Client", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhoneNumber());
    }

    @Test
    public void testAddWithInvalidEmail() {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("invalid-email"); // Invalid email format
        form.setPhoneNumber("1234567890");

        // When
        Set<ConstraintViolation<ClientForm>> violations = validator.validate(form);

        // Then
        assertFalse(violations.isEmpty());
        boolean hasEmailViolation = false;
        for (ConstraintViolation<ClientForm> violation : violations) {
            if (violation.getPropertyPath().toString().equals("email")) {
                hasEmailViolation = true;
                break;
            }
        }
        assertTrue("Should have email validation violation", hasEmailViolation);
    }

    @Test
    public void testInvalidPhoneNumberLength() {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("123456789"); // 9 digits instead of 10

        // When
        Set<ConstraintViolation<ClientForm>> violations = validator.validate(form);

        // Then
        assertFalse(violations.isEmpty());
        boolean hasPhoneViolation = false;
        for (ConstraintViolation<ClientForm> violation : violations) {
            if (violation.getPropertyPath().toString().equals("phoneNumber")) {
                hasPhoneViolation = true;
                break;
            }
        }
        assertTrue("Should have phone number validation violation", hasPhoneViolation);
    }

    @Test
    public void testInvalidPhoneNumberFormat() {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("123abc4567"); // Contains non-numeric characters

        // When
        Set<ConstraintViolation<ClientForm>> violations = validator.validate(form);

        // Then
        assertFalse(violations.isEmpty());
        boolean hasPhoneViolation = false;
        for (ConstraintViolation<ClientForm> violation : violations) {
            if (violation.getPropertyPath().toString().equals("phoneNumber")) {
                hasPhoneViolation = true;
                break;
            }
        }
        assertTrue("Should have phone number validation violation", hasPhoneViolation);
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

        ClientEntity entity1 = new ClientEntity();
        entity1.setId(1L);
        entity1.setName("Test Client 1");
        entity1.setEmail("duplicate@example.com");
        entity1.setPhoneNumber("1234567890");

        when(service.add(any(ClientEntity.class)))
            .thenReturn(entity1)
            .thenThrow(new ApiException("Client with email duplicate@example.com already exists"));

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
        Long id = 1L;
        ClientEntity entity = new ClientEntity();
        entity.setId(id);
        entity.setName("Test Client");
        entity.setEmail("test@example.com");
        entity.setPhoneNumber("1234567890");

        when(service.get(id)).thenReturn(entity);

        // When
        ClientData result = dto.get(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Client", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    public void testGetAll() throws ApiException {
        // Given
        List<ClientEntity> entities = new ArrayList<>();
        
        ClientEntity entity1 = new ClientEntity();
        entity1.setId(1L);
        entity1.setName("Test Client 1");
        entity1.setEmail("test1@example.com");
        entity1.setPhoneNumber("1234567890");
        entities.add(entity1);
        
        ClientEntity entity2 = new ClientEntity();
        entity2.setId(2L);
        entity2.setName("Test Client 2");
        entity2.setEmail("test2@example.com");
        entity2.setPhoneNumber("0987654321");
        entities.add(entity2);
        
        when(service.getAll()).thenReturn(entities);

        // When
        List<ClientData> results = dto.getAll();
        
        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Test Client 1", results.get(0).getName());
        assertEquals("Test Client 2", results.get(1).getName());
    }

    @Test
    public void testUpdate() throws ApiException {
        // Given
        Long id = 1L;
        ClientForm form = new ClientForm();
        form.setName("Updated Client");
        form.setEmail("updated@example.com");
        form.setPhoneNumber("9876543210");
        
        ClientEntity updatedEntity = new ClientEntity();
        updatedEntity.setId(id);
        updatedEntity.setName("Updated Client");
        updatedEntity.setEmail("updated@example.com");
        updatedEntity.setPhoneNumber("9876543210");
        
        when(service.update(any(ClientEntity.class))).thenReturn(updatedEntity);
        
        // When
        ClientData result = dto.update(id, form);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Updated Client", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("9876543210", result.getPhoneNumber());
    }

    @Test
    public void testDelete() throws ApiException {
        // Given
        Long id = 1L;
        doNothing().when(service).delete(id);
        
        // When
        dto.delete(id);
        
        // Then
        verify(service).delete(id);
    }
    
    @Test
    public void testSearch() throws ApiException {
        // Given
        ClientSearchForm searchForm = new ClientSearchForm();
        searchForm.setName("Test");
        
        List<ClientEntity> entities = new ArrayList<>();
        ClientEntity entity = new ClientEntity();
        entity.setId(1L);
        entity.setName("Test Client");
        entity.setEmail("test@example.com");
        entity.setPhoneNumber("1234567890");
        entities.add(entity);
        
        when(service.search(any(ClientSearchForm.class))).thenReturn(entities);
        
        // When
        List<ClientData> results = dto.search(searchForm);
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Client", results.get(0).getName());
    }
} 