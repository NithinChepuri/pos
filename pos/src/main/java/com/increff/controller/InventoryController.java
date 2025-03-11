package com.increff.controller;

import com.increff.model.InventoryData;
import com.increff.model.InventoryForm;
import com.increff.model.InventoryUploadForm;
import com.increff.dto.InventoryDto;
import com.increff.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto dto;

    @ApiOperation(value = "Add inventory")
    @PostMapping
    public InventoryData add(@RequestBody InventoryForm form) throws ApiException {
        return dto.add(form);
    }

    @ApiOperation(value = "Get inventory by ID")
    @GetMapping("/{id}")
    public InventoryData get(@PathVariable Long id) throws ApiException {
        return dto.get(id);
    }

    @ApiOperation(value = "Get all inventory")
    @GetMapping
    public List<InventoryData> getAll() {
        return dto.getAll();
    }

    @ApiOperation(value = "Update inventory")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody InventoryForm form) throws ApiException {
        dto.update(id, form);
    }

    @ApiOperation(value = "Upload Inventory via TSV")
    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file) throws ApiException {
        try {
            List<InventoryUploadForm> forms = readTsvFile(file);
            dto.bulkAdd(forms);
        } catch (Exception e) {
            throw new ApiException("Error processing TSV file: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Search inventory")
    @PostMapping("/search")
    public List<InventoryData> search(@RequestBody InventoryForm form) {
        return dto.search(form);
    }

    @PutMapping("/{productId}")
    public void updateInventory(@PathVariable Long productId, @RequestParam Integer change) throws ApiException {
        dto.validateInventoryUpdate(productId, change);
        dto.updateInventory(productId, change);
    }

    private List<InventoryUploadForm> readTsvFile(MultipartFile file) throws Exception {
        List<InventoryUploadForm> inventoryList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String header = br.readLine();
            int lineNumber = 1;
            String line;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (inventoryList.size() >= 5000) {
                    throw new ApiException("File contains more than 5000 rows");
                }
                
                String[] values = line.split("\t");
                if (values.length != 2) {
                    throw new ApiException("Invalid number of columns at line " + lineNumber);
                }
                
                InventoryUploadForm inventory = new InventoryUploadForm();
                inventory.setBarcode(values[0].trim());
                inventory.setQuantity(values[1].trim());
                inventoryList.add(inventory);
            }
        }
        return inventoryList;
    }
} 