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

    public InventoryData convertEntityToData(InventoryEntity inventory) {
        InventoryData data = new InventoryData();
        data.setId(inventory.getId());
        data.setProductId(inventory.getProductId());
        data.setQuantity(inventory.getQuantity());
        
        try {
            ProductEntity product = productService.get(inventory.getProductId());
            if (product != null) {
                data.setProductName(product.getName());
                data.setBarcode(product.getBarcode());
            }
        } catch (Exception e) {
            // Handle exception if needed
        }
        
        return data;
    }

} 