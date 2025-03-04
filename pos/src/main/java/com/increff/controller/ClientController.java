package com.increff.controller;

import com.increff.model.ClientData;
import com.increff.model.ClientForm;
import com.increff.dto.ClientDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientDto dto;

    @ApiOperation(value = "Add a client")
    @PostMapping
    public ClientData add(@Valid @RequestBody ClientForm form) {
        return dto.add(form);
    }

    @ApiOperation(value = "Get a client by ID")
    @GetMapping("/{id}")
    public ClientData get(@PathVariable Long id) {
        return dto.get(id);
    }

    @ApiOperation(value = "Get list of all clients")
    @GetMapping
    public List<ClientData> getAll() {
        return dto.getAll();
    }

    @ApiOperation(value = "Update a client")
    @PutMapping("/{id}")
    public ClientData update(@PathVariable Long id, @Valid @RequestBody ClientForm form) {
        return dto.update(id, form);
    }

    @ApiOperation(value = "Delete a client")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        dto.delete(id);
    }

    @ApiOperation(value = "Search clients")
    @PostMapping("/search")
    public List<ClientData> search(@RequestBody ClientForm form) {
        return dto.search(form);
    }
}
