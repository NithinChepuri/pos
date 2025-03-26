package com.increff.util;

import com.increff.entity.ClientEntity;
import com.increff.model.clients.ClientData;
import com.increff.model.clients.ClientForm;

public class ClientConverter {

    public static ClientEntity convert(ClientForm form) {
        if (form == null) return null;
        ClientEntity client = new ClientEntity();
        client.setName(form.getName());
        client.setEmail(form.getEmail());
        client.setPhoneNumber(form.getPhoneNumber());
        return client;
    }

    public static ClientData convert(ClientEntity client) {
        if (client == null) return null;
        ClientData data = new ClientData();
        data.setId(client.getId());
        data.setName(client.getName());
        data.setEmail(client.getEmail());
        data.setPhoneNumber(client.getPhoneNumber());
        return data;
    }
} 