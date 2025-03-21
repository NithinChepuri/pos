package com.increff.dto;

import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryForm;
import com.increff.entity.InventoryEntity;
import com.increff.service.InventoryService;
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

@Component
public class InventoryDto {

    @Autowired
    private InventoryService service;
    
    @Autowired
    private InventoryFlow inventoryFlow;

    /**
     * Add a new inventory record
     */
    public InventoryData add(InventoryForm form) throws ApiException {
        validateForm(form);
        InventoryEntity inventory = convertFormToEntity(form);
        service.add(inventory);
        return convertEntityToData(inventory);
    }

    /**
     * Get inventory by ID
     */
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

    /**
     * Get all inventory records with custom pagination
     */
    public List<InventoryData> getAll(int page, int size) {
        List<InventoryEntity> inventories = service.getAll(page, size);
        return convertEntitiesToDataList(inventories);
    }

    /**
     * Update inventory quantity
     */
    public void update(Long id, InventoryForm form) throws ApiException {
        validateForm(form);
        service.update(id, form.getQuantity());
    }

    /**
     * Increase inventory quantity
     */
    public void increaseQuantity(Long id, InventoryForm form) throws ApiException {
        validateForm(form);
        InventoryEntity inventory = getAndValidateInventory(id);
        Long newQuantity = calculateNewQuantity(inventory.getQuantity(), form.getQuantity());
        service.update(id, newQuantity);
    }

    /**
     * Calculate new quantity after increase
     */
    private Long calculateNewQuantity(Long currentQuantity, Long additionalQuantity) {
        return currentQuantity + additionalQuantity;
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
     * Validate inventory form
     */
    private void validateForm(InventoryForm form) throws ApiException {
        if (form == null) {
            throw new ApiException("Form cannot be null");
        }
        if (form.getQuantity() < 0) {
            throw new ApiException("Quantity cannot be negative");
        }
    }

    /**
     * Convert form to entity
     */
    private InventoryEntity convertFormToEntity(InventoryForm form) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(form.getProductId());
        inventory.setQuantity(form.getQuantity());
        return inventory;
    }

    /**
     * Convert entity to data
     */
    private InventoryData convertEntityToData(InventoryEntity inventory) {
        InventoryData data = new InventoryData();
        data.setId(inventory.getId());
        data.setProductId(inventory.getProductId());
        data.setQuantity(inventory.getQuantity());
        return data;
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
    public List<InventoryData> search(InventoryForm form) {
        return search(form, 0, 3);
    }

    /**
     * Search inventory with custom pagination
     */
    public List<InventoryData> search(InventoryForm form, int page, int size) {
        List<InventoryEntity> inventories = service.search(form, page, size);
        return convertEntitiesToDataList(inventories);
    }

    /**
     * Process inventory upload from TSV file
     */
    public ResponseEntity<UploadResponse> processUpload(MultipartFile file) {
        UploadResponse response = new UploadResponse();
        
        try {
            // Parse the TSV file
            List<InventoryUploadForm> forms = readTsvFile(file);
            
            // Process the parsed data
            processInventoryForms(forms, response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Handle any exceptions during file processing
            handleFileProcessingError(e, response);
            return ResponseEntity.badRequest().body(response);
        }
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
            } catch (Exception e) {
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
    private void handleFileProcessingError(Exception e, UploadResponse response) {
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
     * Reads and parses a TSV file into a list of InventoryUploadForm objects
     */
    private List<InventoryUploadForm> readTsvFile(MultipartFile file) throws Exception {
        validateFile(file);
        
        List<InventoryUploadForm> inventoryList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read and validate header
            String header = br.readLine();
            validateHeader(header);
            
            // Process data rows
            processDataRows(br, inventoryList);
        }
        return inventoryList;
    }

    /**
     * Validates the uploaded file
     */
    private void validateFile(MultipartFile file) throws ApiException {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File is empty");
        }
        
        validateFileExtension(file.getOriginalFilename());
    }

    /**
     * Validates file extension is TSV
     */
    private void validateFileExtension(String filename) throws ApiException {
        if (filename == null || !filename.toLowerCase().endsWith(".tsv")) {
            throw new ApiException("Only TSV files are supported");
        }
    }

    /**
     * Validates the header row of the TSV file
     */
    private void validateHeader(String header) throws ApiException {
        if (header == null) {
            throw new ApiException("File is empty");
        }
        
        String[] columns = header.split("\t");
        validateHeaderColumns(columns);
    }

    /**
     * Validates header columns match expected format
     */
    private void validateHeaderColumns(String[] columns) throws ApiException {
        if (columns.length != 2) {
            throw new ApiException("Invalid header format. Expected: Barcode\tQuantity");
        }
        
        if (!columns[0].trim().equalsIgnoreCase("Barcode") || 
            !columns[1].trim().equalsIgnoreCase("Quantity")) {
            throw new ApiException("Invalid header format. Expected: Barcode\tQuantity");
        }
    }

    /**
     * Processes data rows from the BufferedReader and adds them to the inventory list
     */
    private void processDataRows(BufferedReader br, List<InventoryUploadForm> inventoryList) throws Exception {
        String line;
        int lineNumber = 1; // Start after header
        
        while ((line = br.readLine()) != null) {
            lineNumber++;
            
            // Check for maximum rows
            checkMaximumRowsLimit(inventoryList);
            
            // Process the line
            InventoryUploadForm form = parseDataRow(line, lineNumber);
            inventoryList.add(form);
        }
    }

    /**
     * Check if maximum row limit is reached
     */
    private void checkMaximumRowsLimit(List<InventoryUploadForm> inventoryList) throws ApiException {
        if (inventoryList.size() >= 5000) {
            throw new ApiException("File contains more than 5000 rows");
        }
    }

    /**
     * Parses a single data row into an InventoryUploadForm
     */
    private InventoryUploadForm parseDataRow(String line, int lineNumber) throws ApiException {
        String[] values = line.split("\t");
        validateDataRowColumns(values, lineNumber);
        
        InventoryUploadForm inventory = new InventoryUploadForm();
        inventory.setBarcode(values[0].trim());
        inventory.setQuantity(values[1].trim());
        return inventory;
    }

    /**
     * Validates data row has correct number of columns
     */
    private void validateDataRowColumns(String[] values, int lineNumber) throws ApiException {
        if (values.length != 2) {
            throw new ApiException("Invalid number of columns at line " + lineNumber);
        }
    }
} 