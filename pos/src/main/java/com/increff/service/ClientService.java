package com.increff.service;

import com.increff.dao.ClientDao;
import com.increff.entity.ClientEntity;
import com.increff.model.ClientForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClientService {

    @Autowired
    private ClientDao dao;

    public ClientEntity add(ClientEntity client) throws ApiException {
        validateClient(client);
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
        validateClient(client);
        ClientEntity existing = get(client.getId());
        existing.setName(client.getName());
        existing.setEmail(client.getEmail());
        existing.setPhoneNumber(client.getPhoneNumber());
        return dao.update(existing);
    }

    @Transactional
    public void delete(Long id) throws ApiException {
        ClientEntity client = get(id);
        dao.delete(client);
    }

    private void validateClient(ClientEntity client) throws ApiException {
        //ToDo remove trim
        if (client.getName() == null || client.getName().trim().isEmpty()) {
            throw new ApiException("Client name cannot be empty");
        }
        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            throw new ApiException("Client email cannot be empty");
        }

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
    public List<ClientEntity> search(ClientForm form) {
        return dao.search(form);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return dao.select(id) != null;
    }
} 