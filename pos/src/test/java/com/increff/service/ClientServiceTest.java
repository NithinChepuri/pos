package com.increff.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.increff.entity.ClientEntity;
import com.increff.spring.QaConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = QaConfig.class)
@WebAppConfiguration
@Transactional
public class ClientServiceTest {

    @Autowired
    private ClientService service;

    @Test
    public void testAdd() {
        ClientEntity client = new ClientEntity();
        client.setName("Test User");
        client.setEmail("test@example.com");
        client.setPhoneNumber("1234567890");
        
        service.add(client);
        
        assertNotNull(client.getId());
    }

    @Test
    public void testGet() {
        ClientEntity client = new ClientEntity();
        client.setName("Test User");
        client.setEmail("test@example.com");
        client.setPhoneNumber("1234567890");
        service.add(client);
        
        ClientEntity retrieved = service.get(client.getId());
        
        assertNotNull(retrieved);
        assertEquals(client.getName(), retrieved.getName());
        assertEquals(client.getEmail(), retrieved.getEmail());
    }

    @Test
    public void testGetAll() {
        ClientEntity client1 = new ClientEntity();
        client1.setName("User 1");
        client1.setEmail("user1@example.com");
        client1.setPhoneNumber("1234567890");
        service.add(client1);

        ClientEntity client2 = new ClientEntity();
        client2.setName("User 2");
        client2.setEmail("user2@example.com");
        client2.setPhoneNumber("0987654321");
        service.add(client2);
        
        List<ClientEntity> clients = service.getAll();
        
        assertEquals(2, clients.size());
    }

    @Test(expected = RuntimeException.class)
    public void testDuplicateEmail() {
        ClientEntity client1 = new ClientEntity();
        client1.setName("User 1");
        client1.setEmail("same@example.com");
        service.add(client1);

        ClientEntity client2 = new ClientEntity();
        client2.setName("User 2");
        client2.setEmail("same@example.com");
        service.add(client2); // Should throw exception
    }
} 