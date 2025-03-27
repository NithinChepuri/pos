package com.increff.dto;

import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryForm;
import com.increff.entity.InventoryEntity;
import com.increff.service.InventoryService;
import com.increff.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.increff.service.ApiException;
import com.increff.model.inventory.InventoryUploadForm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.increff.model.inventory.UploadResponse;
import com.increff.model.UploadError;
import com.increff.flow.InventoryFlow;
import com.increff.model.inventory.InventorySearchForm;
import com.increff.entity.ProductEntity;
import com.increff.service.ProductService;
import com.increff.model.inventory.InventoryUpdateForm;
import com.increff.util.InventoryTsvUtil;
import java.io.IOException;
import com.increff.util.ValidationUtil;

@Component
public class InventoryDto {

    @Autowired
    private InventoryService service;
    
    @Autowired
    private InventoryFlow inventoryFlow;

   
    public InventoryData add(InventoryForm form) throws ApiException {
        InventoryEntity inventory = ConversionUtil.convertInventoryFormToEntity(form);
        service.add(inventory);
        return convertEntityToData(inventory);
    }

    public InventoryData get(Long id) throws ApiException {
        InventoryEntity inventory = service.get(id);
        ValidationUtil.validateInventoryEntity(inventory, id);
        return convertEntityToData(inventory);
    }


    public List<InventoryData> getAll(int page, int size) {
        List<InventoryEntity> inventories = service.getAll(page, size);
        return convertEntitiesToDataList(inventories);
    }
    
    public void update(Long id, InventoryUpdateForm form) throws ApiException {
        ValidationUtil.validateInventoryUpdateForm(form);
        service.update(id, form.getQuantity());
    }

    /**
     * Convert entity to data
     */
    private InventoryData convertEntityToData(InventoryEntity inventory) {
        // Let the flow layer handle getting product details
        return inventoryFlow.convertEntityToData(inventory);
    }

    /**
     * Convert list of entities to list of data objects
     */
    private List<InventoryData> convertEntitiesToDataList(List<InventoryEntity> inventories) {
        return inventories.stream()
            .map(this::convertEntityToData)
            .collect(Collectors.toList());
    }

    /**
     * Search inventory with default pagination
     */
    public List<InventoryData> search(InventorySearchForm form) {
        return search(form, 0, 3);
    }

    /**
     * Search inventory with custom pagination
     */
    public List<InventoryData> search(InventorySearchForm form, int page, int size) {
        List<InventoryEntity> inventories = service.search(form, page, size);
        return convertEntitiesToDataList(inventories);
    }

    /**
     * Process inventory upload from TSV file
     */
    public ResponseEntity<UploadResponse> processUpload(MultipartFile file) {
        UploadResponse response = new UploadResponse();
        
        try {
            ValidationUtil.validateInventoryFile(file);
            List<InventoryUploadForm> forms = InventoryTsvUtil.readInventoryFromTsv(file);
            
            if (forms.isEmpty()) {
                throw new ApiException("No valid inventory data found in the file");
            }
            
            processInventoryForms(forms, response);
            return ResponseEntity.ok(response);
        } catch (ApiException | IOException e) {
            handleFileProcessingError(e instanceof ApiException ? (ApiException)e : 
                                   new ApiException(e.getMessage()), response);
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void processInventoryForms(List<InventoryUploadForm> forms, UploadResponse response) {
        List<UploadError> errors = new ArrayList<>();
        List<InventoryData> successfulEntries = new ArrayList<>();
        
        ConversionUtil.initializeUploadResponse(response, forms.size());
        
        for (int i = 0; i < forms.size(); i++) {
            InventoryUploadForm form = forms.get(i);
            int lineNumber = i + 2; // +2 because we start after header and 0-indexed list
            try {
                InventoryData data = inventoryFlow.processInventoryForm(form, lineNumber);
                successfulEntries.add(data);
            } catch (ApiException e) {
                errors.add(ConversionUtil.convertToUploadError(lineNumber, form, e.getMessage()));
            }
        }
        
        ConversionUtil.updateUploadResponseResults(response, errors, successfulEntries);
    }

    /**
     * Handles errors that occur during file processing
     */
    private void handleFileProcessingError(ApiException e, UploadResponse response) {
        List<UploadError> errors = new ArrayList<>();
        
        resetUploadResponseCounts(response);
        
        UploadError error = new UploadError(
            0,
            "File processing error",
            e.getMessage()
        );
        errors.add(error);
        
        response.setErrors(errors);
    }

    /**
     * Reset upload response counts to zero
     */
    private void resetUploadResponseCounts(UploadResponse response) {
        response.setTotalRows(0);
        response.setSuccessCount(0);
        response.setErrorCount(0);
    }
} 