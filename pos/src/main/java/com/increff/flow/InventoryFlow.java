package com.increff.flow;

import com.increff.entity.ProductEntity;
import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryUploadForm;
import com.increff.service.ApiException;
import com.increff.service.InventoryService;
import com.increff.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.increff.entity.InventoryEntity;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InventoryFlow {

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ProductService productService;
    
    public InventoryData processInventoryForm(InventoryUploadForm form, int lineNumber) throws ApiException {
        try {
            // Find and validate product
            ProductEntity product = findAndValidateProduct(form.getBarcode());
            
            // Parse and validate quantity
            Long quantity = parseAndValidateQuantity(form.getQuantity(), lineNumber);
            
            // Update inventory and create response
            return createInventoryData(product, quantity);
        } catch (Exception e) {
            handleProcessingError(e, lineNumber);
            return null; // This line won't be reached as handleProcessingError always throws
        }
    }

    /**
     * Find and validate product by barcode
     */
    private ProductEntity findAndValidateProduct(String barcode) throws ApiException {
        ProductEntity product = productService.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product with barcode " + barcode + " not found");
        }
        return product;
    }

    /**
     * Parse and validate quantity
     */
    private Long parseAndValidateQuantity(String quantityStr, int lineNumber) throws ApiException {
        try {
            Long quantity = Long.parseLong(quantityStr);
            if (quantity < 0) {
                throw new ApiException("Quantity cannot be negative at line " + lineNumber);
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid quantity format at line " + lineNumber + ": " + quantityStr);
        }
    }

    /**
     * Create inventory data from product and quantity
     */
    private InventoryData createInventoryData(ProductEntity product, Long quantity) throws ApiException {
        InventoryEntity updatedInventory = inventoryService.updateInventory(product.getId(), quantity);
        
        InventoryData data = new InventoryData();
        data.setId(updatedInventory.getId());
        data.setProductId(product.getId());
        data.setQuantity(quantity);
        data.setProductName(product.getName());
        data.setBarcode(product.getBarcode());
        return data;
    }

    /**
     * Handle processing errors
     */
    private void handleProcessingError(Exception e, int lineNumber) throws ApiException {
        if (e instanceof ApiException) {
            throw (ApiException) e;
        }
        throw new ApiException("Error processing inventory at line " + lineNumber + ": " + e.getMessage());
    }

    public InventoryData convertEntityToData(InventoryEntity inventory) {
        InventoryData data = new InventoryData();
        data.setId(inventory.getId());
        data.setProductId(inventory.getProductId());
        data.setQuantity(inventory.getQuantity());

        ProductEntity product = productService.get(inventory.getProductId());
        if (product != null) {
            data.setProductName(product.getName());
            data.setBarcode(product.getBarcode());
        }

        
        return data;
    }

} 