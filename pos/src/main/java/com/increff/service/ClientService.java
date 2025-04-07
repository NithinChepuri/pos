package com.increff.service;

import com.increff.dao.ClientDao;
import com.increff.entity.ClientEntity;
import com.increff.model.clients.ClientSearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClientService {

    @Autowired
    private ClientDao dao;

    @Transactional
    public ClientEntity add(ClientEntity client) throws ApiException {

        // Check if client with same name exists
        ClientEntity existing = dao.selectByName(client.getName());
        if (existing != null) {
            throw new ApiException("Client with name: " + client.getName() + " already exists");
        }
        validateMailExist(client);
        dao.insert(client);
        return client;
    }

    @Transactional(readOnly = true)
    public ClientEntity get(Long id) throws ApiException {
        ClientEntity client = dao.select(id);
        if (client == null) {
            throw new ApiException("Client with id " + id + " not found");
        }
        return client;
    }

    @Transactional(readOnly = true)
    public List<ClientEntity> getAll() {
        return dao.selectAll();
    }

    @Transactional
    public ClientEntity update(ClientEntity client) throws ApiException {
        // Normalize the client name by trimming spaces
        client.setName(client.getName().trim());
        
        // Check if another client with same name exists
        ClientEntity existing = dao.selectByName(client.getName());
        if (existing != null && !existing.getId().equals(client.getId())) {
            throw new ApiException("Client with name: " + client.getName() + " already exists");
        }
        
        validateMailExist(client);
        ClientEntity toUpdate = get(client.getId());
        toUpdate.setName(client.getName());
        toUpdate.setEmail(client.getEmail());
        toUpdate.setPhoneNumber(client.getPhoneNumber());
        return dao.update(toUpdate);
    }

    @Transactional
    public void delete(Long id) throws ApiException {
        ClientEntity client = get(id);
        dao.delete(client);
    }
    private void validateMailExist(ClientEntity client) throws ApiException {

        // Check for duplicate email (except for updates)
        ClientEntity existing = dao.selectByEmail(client.getEmail());
        if (existing != null && !existing.getId().equals(client.getId())) {
            throw new ApiException("Client with email " + client.getEmail() + " already exists");
        }
    }

    @Transactional(readOnly = true)
    public ClientEntity getByName(String name) {
        return dao.selectByName(name);
    }

    @Transactional(readOnly = true)
    public List<ClientEntity> search(ClientSearchForm form) {
        return dao.search(form);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        try {
            return get(id) != null;
        } catch (ApiException e) {
            return false;
        }
    }
} 