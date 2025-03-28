package com.increff.dao;

import com.increff.entity.ClientEntity;
import com.increff.model.clients.ClientSearchForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientDaoTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ClientEntity> query;

    @Spy
    @InjectMocks
    private ClientDao dao;

    @Before
    public void setUp() {
        // Common setup for TypedQuery mock
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createQuery(anyString(), eq(ClientEntity.class))).thenReturn(query);
    }

    @Test
    public void testInsert() {
        // Given
        ClientEntity client = new ClientEntity();
        client.setName("Test Client");
        client.setEmail("test@example.com");
        client.setPhoneNumber("1234567890");

        // When
        dao.insert(client);

        // Then
        verify(em).persist(client);
    }

    @Test
    public void testSelect() {
        // Given
        Long id = 1L;
        ClientEntity client = new ClientEntity();
        client.setId(id);
        when(em.find(ClientEntity.class, id)).thenReturn(client);

        // When
        ClientEntity result = dao.select(id);

        // Then
        assertNotNull(result);
        verify(em).find(ClientEntity.class, id);
    }

    @Test
    public void testSelectAll() {
        // Given
        List<ClientEntity> expectedClients = Arrays.asList(
            createClient(1L, "Client 1"),
            createClient(2L, "Client 2")
        );

        when(query.getResultList()).thenReturn(expectedClients);

        // When
        List<ClientEntity> results = dao.selectAll();

        // Then
        assertEquals(2, results.size());
        verify(query).getResultList();
    }

    @Test
    public void testSelectByName() {
        // Given
        String name = "Test Client";
        ClientEntity client = createClient(1L, name);
        
        // Use doReturn instead of when for the spy
        doReturn(client).when(dao).getSingleResultOrNull(any(TypedQuery.class));

        // When
        ClientEntity result = dao.selectByName(name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(query).setParameter("name", name);
    }

    @Test
    public void testSelectByEmail() {
        // Given
        String email = "test@example.com";
        ClientEntity client = createClient(1L, "Test Client");
        client.setEmail(email);
        
        // Use doReturn instead of when for the spy
        doReturn(client).when(dao).getSingleResultOrNull(any(TypedQuery.class));

        // When
        ClientEntity result = dao.selectByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(query).setParameter("email", email);
    }

    @Test
    public void testUpdate() {
        // Given
        ClientEntity client = createClient(1L, "Test Client");
        when(em.merge(client)).thenReturn(client);

        // When
        ClientEntity result = dao.update(client);

        // Then
        assertNotNull(result);
        verify(em).merge(client);
    }

    @Test
    public void testDelete() {
        // Given
        ClientEntity client = createClient(1L, "Test Client");
        when(em.merge(client)).thenReturn(client);
        when(em.contains(client)).thenReturn(false);

        // When
        dao.delete(client);

        // Then
        verify(em).remove(any(ClientEntity.class));
    }

    @Test
    public void testSearch() {
        // Given
        ClientSearchForm form = new ClientSearchForm();
        form.setName("Test");
        
        List<ClientEntity> expectedResults = Arrays.asList(
            createClient(1L, "Test Client 1"),
            createClient(2L, "Test Client 2")
        );

        when(query.getResultList()).thenReturn(expectedResults);

        // When
        List<ClientEntity> results = dao.search(form);

        // Then
        assertEquals(2, results.size());
        verify(query).getResultList();
    }

    // Helper method to create test clients
    private ClientEntity createClient(Long id, String name) {
        ClientEntity client = new ClientEntity();
        client.setId(id);
        client.setName(name);
        client.setEmail(name.toLowerCase().replace(" ", "") + "@example.com");
        client.setPhoneNumber("1234567890");
        return client;
    }
}