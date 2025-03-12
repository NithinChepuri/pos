package com.increff.controller;

import com.increff.model.InventoryData;
import com.increff.model.InventoryForm;
import com.increff.model.InventoryUploadForm;
import com.increff.dto.InventoryDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public InventoryData add(@RequestBody InventoryForm form) {
        return dto.add(form);
    }

    @ApiOperation(value = "Get inventory by ID")
    @GetMapping("/{id}")
    public InventoryData get(@PathVariable Long id) {
        return dto.get(id);
    }

    @ApiOperation(value = "Get all inventory")
    @GetMapping
    public List<InventoryData> getAll() {
        return dto.getAll();
    }

    @ApiOperation(value = "Update inventory (Set new value)")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody InventoryForm form) {
        dto.update(id, form);
    }

    @ApiOperation(value = "Update inventory (Add to existing value)")
    @PutMapping("/{id}/add")
    public void updateQuantity(@PathVariable Long id, @RequestBody InventoryForm form) {
        dto.updateQuantity(id, form);
    }

    @ApiOperation(value = "Upload Inventory via TSV")
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        return dto.processUpload(file);
    }

    @ApiOperation(value = "Search inventory")
    @PostMapping("/search")
    public List<InventoryData> search(@RequestBody InventoryForm form) {
        return dto.search(form);
    }
} 