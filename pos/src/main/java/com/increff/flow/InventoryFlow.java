package com.increff.flow;

import com.increff.entity.ProductEntity;
import com.increff.model.inventory.InventoryData;
import com.increff.model.inventory.InventoryForm;
import com.increff.model.inventory.InventoryUploadForm;
import com.increff.model.inventory.InventorySearchForm;
import com.increff.service.ApiException;
import com.increff.service.InventoryService;
import com.increff.service.ProductService;
import com.increff.util.ConversionUtil;
import com.increff.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.increff.entity.InventoryEntity;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InventoryFlow {

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ProductService productService;
    
    public InventoryData processInventoryForm(InventoryUploadForm form, int lineNumber) throws ApiException {
        try {
            ProductEntity product = findAndValidateProduct(form.getBarcode());
            Long quantity = parseAndValidateQuantity(form.getQuantity(), lineNumber);
            return createInventoryData(product, quantity);
        } catch (ApiException e) {
            handleProcessingError(e, lineNumber);
            return null; // This line won't be reached as handleProcessingError always throws
        }
    }
    public List<InventoryData> getAll(int page, int size) {
        List<InventoryEntity> inventories = inventoryService.getAll(page, size);
        return convertEntitiesToDataList(inventories);
    }

    public void update(Long id, Long quantity) throws ApiException {
        inventoryService.update(id, quantity);
    }

    public List<InventoryData> search(InventorySearchForm form, int page, int size) {
        List<InventoryEntity> inventories = inventoryService.search(form, page, size);
        return convertEntitiesToDataList(inventories);
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

    public InventoryData add(InventoryEntity inventory) throws ApiException {
        inventoryService.add(inventory);
        return convertEntityToData(inventory);
    }

    public InventoryEntity get(Long id) throws ApiException {
        return inventoryService.get(id);
    }

    private ProductEntity findAndValidateProduct(String barcode) throws ApiException {
        ProductEntity product = productService.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product with barcode " + barcode + " not found");
        }
        return product;
    }

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

    private void handleProcessingError(Exception e, int lineNumber) throws ApiException {
        if (e instanceof ApiException) {
            throw (ApiException) e;
        }
        throw new ApiException("Error processing inventory at line " + lineNumber + ": " + e.getMessage());
    }
    private List<InventoryData> convertEntitiesToDataList(List<InventoryEntity> inventories) {
        return inventories.stream()
            .map(this::convertEntityToData)
            .collect(Collectors.toList());
    }

} 