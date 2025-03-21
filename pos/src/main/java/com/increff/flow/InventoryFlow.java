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

@Component
@Transactional(rollbackFor = ApiException.class)
public class InventoryFlow {

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ProductService productService;
    
    public InventoryData processInventoryForm(InventoryUploadForm form, int lineNumber) throws ApiException {
        // Find the product by barcode
        ProductEntity product = productService.getByBarcode(form.getBarcode());
        if (product == null) {
            throw new ApiException("Product with barcode " + form.getBarcode() + " not found");
        }
        
        // Parse and validate quantity
        Long quantity = Long.parseLong(form.getQuantity());
        
        // Update inventory
        inventoryService.updateInventory(product.getId(), quantity);
        
        // Create and return inventory data
        InventoryData data = new InventoryData();
        data.setProductId(product.getId());
        data.setQuantity(quantity);
        return data;
    }
    
    public void validateProductExists(String barcode, int lineNumber) throws ApiException {
        ProductEntity product = productService.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Line " + lineNumber + ": Product with barcode " + barcode + " not found");
        }
    }
    
    // Add other methods that require cross-service coordination
} 