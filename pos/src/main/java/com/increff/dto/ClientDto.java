package com.increff.dto;

import com.increff.model.clients.ClientData;
import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientSearchForm;
import com.increff.entity.ClientEntity;
import com.increff.service.ApiException;
import com.increff.service.ClientService;
import com.increff.util.ConversionUtil;
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
        ClientEntity client = ConversionUtil.convertClientFormToEntity(form);
        client.setName(client.getName().trim());
        return ConversionUtil.convertClientEntityToData(service.add(client));
    }

    public ClientData get(Long id) throws ApiException{
        return ConversionUtil.convertClientEntityToData(service.get(id));
    }

    public List<ClientData> getAll() {
        return service.getAll().stream()
                .map(ConversionUtil::convertClientEntityToData)
                .collect(Collectors.toList());
    }

    public ClientData update(Long id, ClientForm form) {
        ClientEntity client = ConversionUtil.convertClientFormToEntity(form);
        client.setId(id);
        return ConversionUtil.convertClientEntityToData(service.update(client));
    }

    public void delete(Long id) {
        service.delete(id);
    }

    public List<ClientData> search(ClientSearchForm form) {
        return service.search(form).stream()
                .map(ConversionUtil::convertClientEntityToData)
                .collect(Collectors.toList());
    }
} 