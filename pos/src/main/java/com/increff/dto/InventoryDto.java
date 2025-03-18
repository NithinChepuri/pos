package com.increff.dto;

import com.increff.model.InventoryData;
import com.increff.model.InventoryForm;
import com.increff.entity.InventoryEntity;
import com.increff.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.increff.service.ApiException;
import com.increff.model.InventoryUploadForm;
import com.increff.util.StringUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.increff.model.UploadResponse;
import com.increff.model.UploadError;
import com.increff.flow.InventoryFlow;

@Component
public class InventoryDto {

    @Autowired
    private InventoryService service;
    
    @Autowired
    private InventoryFlow inventoryFlow;

    public InventoryData add(InventoryForm form) throws ApiException {
        validateForm(form);
        InventoryEntity inventory = convert(form);
        service.add(inventory);
        return convert(inventory);
    }

    public InventoryData get(Long id) throws ApiException {
        InventoryEntity inventory = service.get(id);
        if (inventory == null) {
            throw new ApiException("Inventory not found with id: " + id);
        }
        return convert(inventory);
    }

    public List<InventoryData> getAll() {
        List<InventoryEntity> inventories = service.getAll();
        return inventories.stream().map(this::convert).collect(Collectors.toList());
    }

    public void update(Long id, InventoryForm form) throws ApiException {
        validateForm(form);
        service.update(id, form.getQuantity());
    }

    public void increaseQuantity(Long id, InventoryForm form) throws ApiException {
        validateForm(form);
        InventoryEntity inventory = service.get(id);
        if (inventory == null) {
            throw new ApiException("Inventory not found with id: " + id);
        }
        Long newQuantity = inventory.getQuantity() + form.getQuantity();
        service.update(id, newQuantity);
    }

    private void validateForm(InventoryForm form) throws ApiException {
        if (form == null) {
            throw new ApiException("Form cannot be null");
        }
        if (form.getQuantity() < 0) {
            throw new ApiException("Quantity cannot be negative");
        }
    }

    private InventoryEntity convert(InventoryForm form) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(form.getProductId());
        inventory.setQuantity(form.getQuantity());
        return inventory;
    }

    private InventoryData convert(InventoryEntity inventory) {
        InventoryData data = new InventoryData();
        data.setId(inventory.getId());
        data.setProductId(inventory.getProductId());
        data.setQuantity(inventory.getQuantity());
        return data;
    }

    public List<InventoryData> search(InventoryForm form) {
        List<InventoryEntity> inventories = service.search(form);
        return inventories.stream().map(this::convert).collect(Collectors.toList());
    }

    public ResponseEntity<UploadResponse> processUpload(MultipartFile file) {
        UploadResponse response = new UploadResponse();
        
        try {
            // Step 1: Parse the TSV file
            List<InventoryUploadForm> forms = readTsvFile(file);
            
            // Step 2: Process the parsed data
            processInventoryForms(forms, response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Step 3: Handle any exceptions during file processing
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
        int successCount = 0;
        
        response.setTotalRows(forms.size());
        
        // Process each form entry
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
        
        // Update response with results
        response.setSuccessCount(successCount);
        response.setErrorCount(forms.size() - successCount);
        response.setErrors(errors);
        response.setSuccessfulEntries(successfulEntries);
    }

    /**
     * Validates an inventory upload form
     */
    private void validateUploadForm(InventoryUploadForm form, int lineNumber) throws ApiException {
        validateBarcodeField(form.getBarcode(), lineNumber);
        validateQuantityField(form.getQuantity(), lineNumber);
        validateProductExists(form.getBarcode(), lineNumber);
    }

    /**
     * Validates that the barcode field is not empty
     */
    private void validateBarcodeField(String barcode, int lineNumber) throws ApiException {
        if (StringUtil.isEmpty(barcode)) {
            throw new ApiException("Line " + lineNumber + ": Barcode cannot be empty");
        }
        
        // Additional barcode validation could be added here
        // For example, checking format, length, etc.
    }

    /**
     * Validates that the quantity field is a valid positive number
     */
    private void validateQuantityField(String quantityStr, int lineNumber) throws ApiException {
        if (StringUtil.isEmpty(quantityStr)) {
            throw new ApiException("Line " + lineNumber + ": Quantity cannot be empty");
        }
        
        try {
            Long quantity = Long.parseLong(quantityStr);
            if (quantity < 0) {
                throw new ApiException("Line " + lineNumber + ": Quantity cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new ApiException("Line " + lineNumber + ": Invalid quantity format - must be a number");
        }
    }

    /**
     * Validates that the product with the given barcode exists
     */
    private void validateProductExists(String barcode, int lineNumber) throws ApiException {
        inventoryFlow.validateProductExists(barcode, lineNumber);
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
        
        response.setTotalRows(0);
        response.setSuccessCount(0);
        response.setErrorCount(0);
        
        UploadError error = new UploadError(
            0,
            "File processing error",
            e.getMessage()
        );
        errors.add(error);
        
        response.setErrors(errors);
    }

    /**
     * Reads and parses a TSV file into a list of InventoryUploadForm objects
     */
    private List<InventoryUploadForm> readTsvFile(MultipartFile file) throws Exception {
        validateFile(file);
        
        List<InventoryUploadForm> inventoryList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip header
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
        
        String filename = file.getOriginalFilename();
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
            if (inventoryList.size() >= 5000) {
                throw new ApiException("File contains more than 5000 rows");
            }
            
            // Process the line
            InventoryUploadForm form = parseDataRow(line, lineNumber);
            inventoryList.add(form);
        }
    }

    /**
     * Parses a single data row into an InventoryUploadForm
     */
    private InventoryUploadForm parseDataRow(String line, int lineNumber) throws ApiException {
        String[] values = line.split("\t");
        if (values.length != 2) {
            throw new ApiException("Invalid number of columns at line " + lineNumber);
        }
        
        InventoryUploadForm inventory = new InventoryUploadForm();
        inventory.setBarcode(values[0].trim());
        inventory.setQuantity(values[1].trim());
        return inventory;
    }
} 