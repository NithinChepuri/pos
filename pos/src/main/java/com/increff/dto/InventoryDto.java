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
//todo : call getchecks
    public InventoryData get(Long id) throws ApiException {
        InventoryEntity inventory = getAndValidateInventory(id);
        return convertEntityToData(inventory);
    }

    /**
     * Get all inventory records with default pagination
     */
    public List<InventoryData> getAll() {
        return getAll(0, 3);
    }

    public List<InventoryData> getAll(int page, int size) {
        List<InventoryEntity> inventories = service.getAll(page, size);
        return convertEntitiesToDataList(inventories);
    }
    
    public void update(Long id, InventoryUpdateForm form) throws ApiException {
        if (form == null) {
            throw new ApiException("Form cannot be null");
        }
        if (form.getQuantity() < 0) {
            throw new ApiException("Quantity cannot be negative");
        }
        service.update(id, form.getQuantity());
    }

    /**
     * Get and validate inventory exists
     */
    private InventoryEntity getAndValidateInventory(Long id) throws ApiException {
        InventoryEntity inventory = service.get(id);
        if (inventory == null) {
            throw new ApiException("Inventory not found with id: " + id);
        }
        return inventory;
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
            // First part: validate and parse the file
            List<InventoryUploadForm> forms = validateAndParseTsvFile(file);
            
            // Second part: process the parsed data
            processInventoryForms(forms, response);
            
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            // Handle any exceptions during file processing
            handleFileProcessingError(e, response);
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            // Handle any exceptions during file processing
            handleFileProcessingError(new ApiException(e.getMessage()), response);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Validates and parses the TSV file
     */
    private List<InventoryUploadForm> validateAndParseTsvFile(MultipartFile file) throws ApiException, IOException {
        // Validate file
        validateFile(file);
        
        // Parse the TSV file
        List<InventoryUploadForm> forms = InventoryTsvUtil.readInventoryFromTsv(file);
        
        if (forms.isEmpty()) {
            throw new ApiException("No valid inventory data found in the file");
        }
        
        return forms;
    }

    /**
     * Processes a list of inventory forms and updates the response object
     */
    private void processInventoryForms(List<InventoryUploadForm> forms, UploadResponse response) {
        List<UploadError> errors = new ArrayList<>();
        List<InventoryData> successfulEntries = new ArrayList<>();
        
        initializeUploadResponse(response, forms.size());
        processEachInventoryForm(forms, response, errors, successfulEntries);
        updateUploadResponseResults(response, errors, successfulEntries);
    }

    /**
     * Initialize upload response with total rows
     */
    private void initializeUploadResponse(UploadResponse response, int totalRows) {
        response.setTotalRows(totalRows);
    }

    /**
     * Process each inventory form in the upload
     */
    private void processEachInventoryForm(
            List<InventoryUploadForm> forms, 
            UploadResponse response,
            List<UploadError> errors,
            List<InventoryData> successfulEntries) {
        
        int successCount = 0;
        
        for (int i = 0; i < forms.size(); i++) {
            InventoryUploadForm form = forms.get(i);
            int lineNumber = i + 2; // +2 because we start after header and 0-indexed list
            try {
                // Process a single inventory form - use the flow instead of direct service calls
                InventoryData data = inventoryFlow.processInventoryForm(form, lineNumber);
                successfulEntries.add(data);
                successCount++;
            } catch (ApiException e) {
                // Add to errors
                errors.add(createUploadError(lineNumber, form, e.getMessage()));
            }
        }
        
        response.setSuccessCount(successCount);
        response.setErrorCount(forms.size() - successCount);
    }

    /**
     * Update upload response with results
     */
    private void updateUploadResponseResults(
            UploadResponse response, 
            List<UploadError> errors,
            List<InventoryData> successfulEntries) {
        
        response.setErrors(errors);
        response.setSuccessfulEntries(successfulEntries);
    }

    /**
     * Creates an UploadError object for a failed inventory form
     */
    private UploadError createUploadError(int lineNumber, InventoryUploadForm form, String errorMessage) {
        return new UploadError(
            lineNumber,
            "Barcode: " + form.getBarcode() + ", Quantity: " + form.getQuantity(),
            errorMessage
        );
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

    /**
     * Validates the uploaded file
     */
    private void validateFile(MultipartFile file) throws ApiException {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".tsv")) {
            throw new ApiException("Only TSV files are supported");
        }
    }
} 