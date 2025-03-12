package com.increff.dto;

import com.increff.model.InventoryData;
import com.increff.model.InventoryForm;
import com.increff.entity.InventoryEntity;
import com.increff.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.increff.service.ApiException;
import com.increff.model.InventoryUploadForm;
import com.increff.service.ProductService;
import com.increff.util.StringUtil;
import com.increff.entity.ProductEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.increff.model.UploadResponse;
import com.increff.model.UploadError;

@Component
public class InventoryDto {

    @Autowired
    private InventoryService service;
    
    @Autowired
    private ProductService productService;

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

    public void updateQuantity(Long id, InventoryForm form) throws ApiException {
        validateForm(form);
        InventoryEntity inventory = service.get(id);
        if (inventory == null) {
            throw new ApiException("Inventory not found with id: " + id);
        }
        Long newQuantity = inventory.getQuantity() + form.getQuantity();
        service.update(id, newQuantity);
    }

    public void bulkAdd(List<InventoryUploadForm> forms) throws ApiException {
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;
        
        for (InventoryUploadForm form : forms) {
            lineNumber++;
            try {
                validateUploadForm(form, lineNumber);
                ProductEntity product = productService.getByBarcode(form.getBarcode());
                if (product == null) {
                    throw new ApiException("Product with barcode " + form.getBarcode() + " not found");
                }
                
                Long quantity = Long.parseLong(form.getQuantity());
                service.updateInventory(product.getId(), quantity);
            } catch (Exception e) {
                errors.add("Error at line " + lineNumber + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ApiException("Errors in TSV file:\n" + String.join("\n", errors));
        }
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

    private void validateUploadForm(InventoryUploadForm form, int lineNumber) throws ApiException {
        if (StringUtil.isEmpty(form.getBarcode())) {
            throw new ApiException("Barcode cannot be empty");
        }
        try {
            Long quantity = Long.parseLong(form.getQuantity());
            if (quantity < 0) {
                throw new ApiException("Quantity cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid quantity format");
        }
    }

    public void updateInventory(Long productId, Long change) throws ApiException {
        validateInventoryUpdate(productId, change);
        service.updateInventory(productId, change);
    }

    public void validateInventoryUpdate(Long productId, Long change) throws ApiException {
        InventoryEntity inventory = service.getByProductId(productId);
        
        if (inventory == null) {
            if (change < 0) {
                throw new ApiException("Cannot reduce inventory below 0");
            }
            return;
        }
        
        long newQuantity = inventory.getQuantity() + change;
        if (newQuantity < 0) {
            throw new ApiException("Cannot reduce inventory below 0");
        }
    }

    public ResponseEntity<UploadResponse> processUpload(MultipartFile file) {
        UploadResponse response = new UploadResponse();
        List<UploadError> errors = new ArrayList<>();
        List<InventoryData> successfulEntries = new ArrayList<>();
        
        try {
            List<InventoryUploadForm> forms = readTsvFile(file);
            response.setTotalRows(forms.size());
            
            int successCount = 0;
            int lineNumber = 1; // Start after header
            
            for (InventoryUploadForm form : forms) {
                lineNumber++;
                try {
                    validateUploadForm(form, lineNumber);
                    ProductEntity product = productService.getByBarcode(form.getBarcode());
                    if (product == null) {
                        throw new ApiException("Product with barcode " + form.getBarcode() + " not found");
                    }
                    
                    Long quantity = Long.parseLong(form.getQuantity());
                    service.updateInventory(product.getId(), quantity);
                    
                    // Add to successful entries
                    InventoryData data = new InventoryData();
                    data.setProductId(product.getId());
                    data.setQuantity(quantity);
                    successfulEntries.add(data);
                    successCount++;
                } catch (Exception e) {
                    // Add to errors
                    UploadError error = new UploadError(
                        lineNumber,
                        "Barcode: " + form.getBarcode() + ", Quantity: " + form.getQuantity(),
                        e.getMessage()
                    );
                    errors.add(error);
                }
            }
            
            response.setSuccessCount(successCount);
            response.setErrorCount(forms.size() - successCount);
            response.setErrors(errors);
            response.setSuccessfulEntries(successfulEntries);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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
            return ResponseEntity.badRequest().body(response);
        }
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