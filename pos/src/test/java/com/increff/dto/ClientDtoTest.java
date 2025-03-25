package com.increff.dto;

import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientData;
import com.increff.service.ApiException;
import com.increff.spring.AbstractUnitTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import static org.junit.Assert.*;

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

    @Test(expected = ApiException.class)
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

        // When
        dto.add(form1);
        dto.add(form2); // Should throw ApiException
    }


    @Test(expected = ApiException.class)
    public void testValidPhoneNumber() throws ApiException{
        ClientForm form1 = new ClientForm();
        form1.setName("Test Client 1");
        form1.setEmail("duplicate@example.com");
        form1.setPhoneNumber("123456789");
        dto.add(form1);
    }

    @Test(expected = ApiException.class)
    public void testInvalidPhoneNumberLength() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("123456789"); // 9 digits instead of 10

        // When - should throw ApiException
        dto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testInvalidPhoneNumberFormat() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("123abc4567"); // Contains non-numeric characters

        // When - should throw ApiException
        dto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testAddWithInvalidEmail() throws ApiException {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("invalid-email");
        form.setPhoneNumber("1234567890");

        dto.add(form);
    }
    @Test
    public void testGetAll() throws ApiException {
        // Given
        ClientForm form1 = new ClientForm();
        form1.setName("Test Client 1");
        form1.setEmail("test1@example.com");
        form1.setPhoneNumber("1234567890");

        ClientForm form2 = new ClientForm();
        form2.setName("Test Client 2");
        form2.setEmail("test2@example.com");
        form2.setPhoneNumber("0987654321"); 
        
        dto.add(form1);
        dto.add(form2);

        // When
        List<ClientData> result = dto.getAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Client 1", result.get(0).getName());
        assertEquals("test1@example.com", result.get(0).getEmail());
        assertEquals("1234567890", result.get(0).getPhoneNumber());        
    }
    //test case for update
    @Test
    public void testUpdate() throws ApiException {
        // Given
        ClientForm form1 = new ClientForm();
        form1.setName("Test Client 1");
        form1.setEmail("test1@example.com");
        form1.setPhoneNumber("1234567890");

        ClientForm form2 = new ClientForm();
        form2.setName("Test Client 2");
        form2.setEmail("test2@example.com");
        form2.setPhoneNumber("0987654321"); 
        
        // When
        ClientData addedClient = dto.add(form1);  // Get the added client data
        ClientData result = dto.update(addedClient.getId(), form2);  // Use the ID from added client
        
        // Then
        assertNotNull(result);
        assertEquals("Test Client 2", result.getName());
        assertEquals("test2@example.com", result.getEmail());
        assertEquals("0987654321", result.getPhoneNumber());
    }
    //test case for delete
    @Test
    public void testDelete() throws ApiException {
        // Given
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setEmail("test@example.com");
        form.setPhoneNumber("1234567890");

        // When
        ClientData addedClient = dto.add(form);  // Get the added client data
        dto.delete(addedClient.getId());  // Use the ID from added client

        // Then
        // Verify that the client is deleted by trying to get it (should throw exception)
        try {
            dto.get(addedClient.getId());
            fail("Expected ApiException was not thrown");
        } catch (ApiException e) {
            assertEquals("Client with id " + addedClient.getId() + " not found", e.getMessage());
        }
    }   
    
} 