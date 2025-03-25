package com.increff.service;

import com.increff.dao.ClientDao;
import com.increff.entity.ClientEntity;
import com.increff.model.clients.ClientSearchForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientServiceTest {

    @Mock
    private ClientDao dao;

    @InjectMocks
    private ClientService service;

    // Test Add Operations
    @Test
    public void testAddValidClient() throws ApiException {
        // Given
        ClientEntity client = new ClientEntity();
        client.setName("Test Client");
        client.setEmail("test@example.com");
        client.setPhoneNumber("1234567890");

        when(dao.selectByName(anyString())).thenReturn(null);
        when(dao.selectByEmail(anyString())).thenReturn(null);

        // When
        ClientEntity result = service.add(client);

        // Then
        assertNotNull(result);
        verify(dao).insert(client);
    }

    @Test(expected = ApiException.class)
    public void testAddDuplicateName() throws ApiException {
        // Given
        ClientEntity existingClient = new ClientEntity();
        existingClient.setName("Test Client");
        
        ClientEntity newClient = new ClientEntity();
        newClient.setName("Test Client");
        newClient.setEmail("test@example.com");

        when(dao.selectByName("Test Client")).thenReturn(existingClient);

        // When/Then
        service.add(newClient); // Should throw ApiException
    }

    @Test(expected = ApiException.class)
    public void testAddDuplicateEmail() throws ApiException {
        // Given
        ClientEntity existingClient = new ClientEntity();
        existingClient.setId(1L);
        existingClient.setName("Existing Client");
        existingClient.setEmail("test@example.com");
        existingClient.setPhoneNumber("1234567890");
        
        ClientEntity newClient = new ClientEntity();
        newClient.setName("New Client");
        newClient.setEmail("test@example.com");
        newClient.setPhoneNumber("0987654321");

        when(dao.selectByEmail("test@example.com")).thenReturn(existingClient);
        when(dao.selectByName(anyString())).thenReturn(null);

        // When/Then
        service.add(newClient); // Should throw ApiException
    }

    // Test Get Operations
    @Test
    public void testGetExistingClient() throws ApiException {
        // Given
        Long id = 1L;
        ClientEntity client = new ClientEntity();
        client.setId(id);
        client.setName("Test Client");

        when(dao.select(id)).thenReturn(client);

        // When
        ClientEntity result = service.get(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Client", result.getName());
    }

    @Test(expected = ApiException.class)
    public void testGetNonExistentClient() throws ApiException {
        // Given
        Long id = 1L;
        when(dao.select(id)).thenReturn(null);

        // When/Then
        service.get(id); // Should throw ApiException
    }

    // Test GetAll Operation
    @Test
    public void testGetAll() {
        // Given
        List<ClientEntity> clients = Arrays.asList(
            createClient(1L, "Client 1", "client1@example.com"),
            createClient(2L, "Client 2", "client2@example.com")
        );
        when(dao.selectAll()).thenReturn(clients);

        // When
        List<ClientEntity> result = service.getAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Client 1", result.get(0).getName());
        assertEquals("Client 2", result.get(1).getName());
    }

    // Test Update Operations
    @Test
    public void testUpdateValidClient() throws ApiException {
        // Given
        Long id = 1L;
        ClientEntity existingClient = createClient(id, "Old Name", "old@example.com");
        ClientEntity updatedClient = createClient(id, "New Name", "new@example.com");

        when(dao.select(id)).thenReturn(existingClient);
        when(dao.selectByName(anyString())).thenReturn(null);
        when(dao.selectByEmail(anyString())).thenReturn(null);
        when(dao.update(any(ClientEntity.class))).thenReturn(updatedClient);

        // When
        ClientEntity result = service.update(updatedClient);

        // Then
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test(expected = ApiException.class)
    public void testUpdateNonExistentClient() throws ApiException {
        // Given
        Long id = 1L;
        ClientEntity client = createClient(id, "Test", "test@example.com");
        when(dao.select(id)).thenReturn(null);

        // When/Then
        service.update(client); // Should throw ApiException
    }

    // Test Delete Operations
    @Test
    public void testDeleteExistingClient() throws ApiException {
        // Given
        Long id = 1L;
        ClientEntity client = createClient(id, "Test", "test@example.com");
        when(dao.select(id)).thenReturn(client);

        // When
        service.delete(id);

        // Then
        verify(dao).delete(client);
    }

    @Test(expected = ApiException.class)
    public void testDeleteNonExistentClient() throws ApiException {
        // Given
        Long id = 1L;
        when(dao.select(id)).thenReturn(null);

        // When/Then
        service.delete(id); // Should throw ApiException
    }

    // Test Search Operations
    @Test
    public void testSearch() {
        // Given
        ClientSearchForm form = new ClientSearchForm();
        form.setName("Test");
        form.setEmail("test@example.com");

        List<ClientEntity> expectedResults = Arrays.asList(
            createClient(1L, "Test 1", "test1@example.com"),
            createClient(2L, "Test 2", "test2@example.com")
        );
        when(dao.search(form)).thenReturn(expectedResults);

        // When
        List<ClientEntity> results = service.search(form);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(dao).search(form);
    }

    // Helper method to create client entities
    private ClientEntity createClient(Long id, String name, String email) {
        ClientEntity client = new ClientEntity();
        client.setId(id);
        client.setName(name);
        client.setEmail(email);
        client.setPhoneNumber("1234567890");
        return client;
    }
} 