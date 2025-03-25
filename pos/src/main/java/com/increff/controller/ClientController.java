package com.increff.controller;

import com.increff.model.clients.ClientData;
import com.increff.model.clients.ClientForm;
import com.increff.model.clients.ClientSearchForm;
import com.increff.dto.ClientDto;
import com.increff.model.enums.Role;
import com.increff.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
    public ClientData add(@Valid @RequestBody ClientForm form, HttpServletRequest request) throws ApiException {
        checkSupervisorAccess(request);
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
    public ClientData update(@PathVariable Long id, @Valid @RequestBody ClientForm form, HttpServletRequest request) throws ApiException {
        checkSupervisorAccess(request);
        return dto.update(id, form);
    }

    @ApiOperation(value = "Delete a client")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) throws ApiException {
        checkSupervisorAccess(request);
        dto.delete(id);
    }

    @ApiOperation(value = "Search clients")
    @PostMapping("/search")
    public List<ClientData> search(@RequestBody ClientSearchForm form) {
        return dto.search(form);
    }
    
    /**
     * Check if the current user has supervisor access
     */
    private void checkSupervisorAccess(HttpServletRequest request) throws ApiException {
        HttpSession session = request.getSession();
        Role role = (Role) session.getAttribute("role");
        
        if (role != Role.SUPERVISOR) {
            throw new ApiException("Access denied. Supervisor role required.");
        }
    }
}
