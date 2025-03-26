package com.increff.dto;

import com.increff.model.clients.ClientData;
import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientSearchForm;
import com.increff.entity.ClientEntity;
import com.increff.service.ApiException;
import com.increff.service.ClientService;
import com.increff.util.ClientConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.Validation;
import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientDto {

    @Autowired
    private ClientService service;

    public ClientData add(ClientForm form) throws ApiException {
        
        ClientEntity client = ClientConverter.convert(form);
        client.setName(client.getName().trim());
        return ClientConverter.convert(service.add(client));
    }



    public ClientData get(Long id) throws ApiException{
        return ClientConverter.convert(service.get(id));
    }

    public List<ClientData> getAll() {
        return service.getAll().stream()
                .map(ClientConverter::convert)
                .collect(Collectors.toList());
    }

    public ClientData update(Long id, ClientForm form) {
        ClientEntity client = ClientConverter.convert(form);
        client.setId(id);
        return ClientConverter.convert(service.update(client));
    }

    public void delete(Long id) {
        service.delete(id);
    }

    public List<ClientData> search(ClientSearchForm form) {
        return service.search(form).stream()
                .map(ClientConverter::convert)
                .collect(Collectors.toList());
    }
} 