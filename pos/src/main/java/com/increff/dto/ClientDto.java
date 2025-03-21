package com.increff.dto;

import com.increff.model.clients.ClientData;
import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientSearchForm;
import com.increff.entity.ClientEntity;
import com.increff.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClientDto {

    @Autowired
    private ClientService service;

    public ClientData add(ClientForm form) {
        ClientEntity client = convert(form);
        client.setName(client.getName().trim());
        return convert(service.add(client));
    }

    public ClientData get(Long id) {
        return convert(service.get(id));
    }

    public List<ClientData> getAll() {
        List<ClientData> list = new ArrayList<>();
        for (ClientEntity client : service.getAll()) {
            list.add(convert(client));
        }
        return list;
    }

    public ClientData update(Long id, ClientForm form) {
        ClientEntity client = convert(form);
        client.setId(id);
        return convert(service.update(client));
    }

    public void delete(Long id) {
        service.delete(id);
    }

    public List<ClientData> search(ClientSearchForm form) {
        List<ClientEntity> clients = service.search(form);
        List<ClientData> list = new ArrayList<>();
        for (ClientEntity client : clients) {
            list.add(convert(client));
        }
        return list;
    }

    private ClientEntity convert(ClientForm form) {
        ClientEntity client = new ClientEntity();
        client.setName(form.getName());
        client.setEmail(form.getEmail());
        client.setPhoneNumber(form.getPhoneNumber());
        return client;
    }

    private ClientData convert(ClientEntity client) {
        if (client == null) return null;
        ClientData data = new ClientData();
        data.setId(client.getId());
        data.setName(client.getName());
        data.setEmail(client.getEmail());
        data.setPhoneNumber(client.getPhoneNumber());
        return data;
    }
} 