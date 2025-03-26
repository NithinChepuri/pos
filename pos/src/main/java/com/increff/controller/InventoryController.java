package com.increff.controller;

import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventorySearchForm;
import com.increff.dto.InventoryDto;
import com.increff.util.AuthorizationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.increff.model.inventory.UploadResponse;
import com.increff.model.enums.Role;
import com.increff.service.ApiException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto dto;

    @ApiOperation(value = "Add inventory")
    @PostMapping
    public InventoryData add(@Valid @RequestBody InventoryForm form, HttpServletRequest request) throws ApiException {
        AuthorizationUtil.checkSupervisorAccess(request);
        return dto.add(form);
    }

    @ApiOperation(value = "Get inventory by ID")
    @GetMapping("/{id}")
    public InventoryData get(@PathVariable Long id) {
        return dto.get(id);
    }

    @ApiOperation(value = "Get all inventory")
    @GetMapping
    public List<InventoryData> getAll(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
        return dto.getAll(page, size);
    }

    @ApiOperation(value = "Update inventory (Set new value)")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody InventoryForm form, HttpServletRequest request) throws ApiException {
        AuthorizationUtil.checkSupervisorAccess(request);
        dto.update(id, form);
    }

    @ApiOperation(value = "Upload Inventory via TSV")
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws ApiException {
        AuthorizationUtil.checkSupervisorAccess(request);
        return dto.processUpload(file);
    }

    @ApiOperation(value = "Search inventory with pagination")
    @PostMapping("/search")
    public List<InventoryData> search(
        @RequestBody InventorySearchForm form,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
        return dto.search(form, page, size);
    }
    

} 