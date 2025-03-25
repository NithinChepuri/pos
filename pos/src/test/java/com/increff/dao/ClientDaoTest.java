package com.increff.dao;

import com.increff.entity.ClientEntity;
import com.increff.model.clients.ClientSearchForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @InjectMocks
    private ClientDao dao;

    @Before
    public void setUp() {
        // Common setup for TypedQuery mock
        when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    public void testInsert() {
        
        ClientEntity client = new ClientEntity();
        client.setName("Test Client");
        client.setEmail("test@example.com");
        client.setPhoneNumber("1234567890");

        
        dao.insert(client);

        
        verify(em).persist(client);
    }

    @Test
    public void testSelect() {
        
        Long id = 1L;
        ClientEntity client = new ClientEntity();
        client.setId(id);
        when(em.find(ClientEntity.class, id)).thenReturn(client);

        
        ClientEntity result = dao.select(id);

        
        assertNotNull(result);
        verify(em).find(ClientEntity.class, id);
    }

    @Test
    public void testSelectAll() {
        
        List<ClientEntity> expectedClients = Arrays.asList(
            createClient(1L, "Client 1"),
            createClient(2L, "Client 2")
        );

        when(em.createQuery(anyString(), eq(ClientEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedClients);

        
        List<ClientEntity> results = dao.selectAll();

        
        assertEquals(2, results.size());
        verify(query).getResultList();
    }

    @Test
    public void testSelectByName() {
        
        String name = "Test Client";
        ClientEntity client = createClient(1L, name);
        List<ClientEntity> clients = Arrays.asList(client);
        
        when(em.createQuery(anyString(), eq(ClientEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(clients);

        
        ClientEntity result = dao.selectByName(name);

        
        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(query).setParameter("name", name);
        verify(query).getResultList();
    }

    @Test
    public void testSelectByEmail() {
        
        String email = "test@example.com";
        ClientEntity client = createClient(1L, "Test Client");
        client.setEmail(email);
        List<ClientEntity> clients = Arrays.asList(client);
        
        when(em.createQuery(anyString(), eq(ClientEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(clients);

        
        ClientEntity result = dao.selectByEmail(email);

        
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(query).setParameter("email", email);
        verify(query).getResultList();
    }

    @Test
    public void testUpdate() {
        
        ClientEntity client = createClient(1L, "Test Client");

        
        dao.update(client);

        
        verify(em).merge(client);
    }

    @Test
    public void testDelete() {
        
        ClientEntity client = createClient(1L, "Test Client");
        when(em.merge(client)).thenReturn(client);

        
        dao.delete(client);

        
        verify(em).remove(any(ClientEntity.class));
    }

    @Test
    public void testSearch() {
        
        ClientSearchForm form = new ClientSearchForm();
        form.setName("Test");
        
        List<ClientEntity> expectedResults = Arrays.asList(
            createClient(1L, "Test Client 1"),
            createClient(2L, "Test Client 2")
        );

        when(em.createQuery(anyString(), eq(ClientEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedResults);

        
        List<ClientEntity> results = dao.search(form);

        
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