package com.increff.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.increff.model.ClientData;
import com.increff.model.ClientForm;
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
public class ClientDtoTest {

    @Autowired
    private ClientDto dto;

    @Test
    public void testAdd() {
        ClientForm form = new ClientForm();
        form.setName("Test User");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");
        
        ClientData data = dto.add(form);
        
        assertNotNull(data.getId());
        assertEquals(form.getName(), data.getName());
        assertEquals(form.getEmail(), data.getEmail());
    }

    @Test
    public void testGetAll() {
        ClientForm form1 = new ClientForm();
        form1.setName("User 1");
        form1.setEmail("user1@example.com");
        form1.setPhoneNumber("1234567890");
        dto.add(form1);

        ClientForm form2 = new ClientForm();
        form2.setName("User 2");
        form2.setEmail("user2@example.com");
        form2.setPhoneNumber("0987654321");
        dto.add(form2);
        
        List<ClientData> clients = dto.getAll();
        
        assertEquals(2, clients.size());
    }
} 