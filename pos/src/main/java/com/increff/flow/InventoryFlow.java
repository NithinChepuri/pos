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
            // Find the product by barcode
            ProductEntity product = productService.getByBarcode(form.getBarcode());
            if (product == null) {
                throw new ApiException("Product with barcode " + form.getBarcode() + " not found");
            }
            
            // Parse and validate quantity
            Long quantity;
            try {
                quantity = Long.parseLong(form.getQuantity());
                if (quantity < 0) {
                    throw new ApiException("Quantity cannot be negative at line " + lineNumber);
                }
            } catch (NumberFormatException e) {
                throw new ApiException("Invalid quantity format at line " + lineNumber + ": " + form.getQuantity());
            }
            
            // Update inventory
            InventoryEntity updatedInventory = inventoryService.updateInventory(product.getId(), quantity);
            
            // Create and return inventory data
            InventoryData data = new InventoryData();
            data.setId(updatedInventory.getId());
            data.setProductId(product.getId());
            data.setQuantity(quantity);
            data.setProductName(product.getName());
            data.setBarcode(product.getBarcode());
            return data;
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException("Error processing inventory at line " + lineNumber + ": " + e.getMessage());
        }
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