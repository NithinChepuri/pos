package com.increff.dto;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.flow.ProductFlow;
import com.increff.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.increff.model.Constants.MAX_BARCODE_LENGTH;

@Component
public class ProductDto {

    @Autowired
    private ProductFlow flow;
    
    // Maximum allowed barcode length
//    private static final int MAX_BARCODE_LENGTH = 50;

    public ProductData add(ProductForm form) throws ApiException {
        // Validate form
        validateForm(form);
        
        // Convert form to entity
        ProductEntity entity = convertFormToEntity(form);
        
        // Delegate to flow layer
        return flow.add(entity);
    }

    public ProductData get(Long id) throws ApiException {
        return flow.get(id);
    }

    public List<ProductData> getAll(int page, int size) {
        return flow.getAll(page, size);
    }

    public ProductData update(Long id, ProductForm form) throws ApiException {
        // Validate form
        validateForm(form);
        
        // Convert form to entity
        ProductEntity entity = convertFormToEntity(form);
        
        // Delegate to flow layer
        return flow.update(id, entity);
    }

    public List<ProductData> search(ProductSearchForm form, int page, int size) {
        return flow.search(form, page, size);
    }

    public void delete(Long id) throws ApiException {
        flow.delete(id);
    }

    public UploadResult<ProductData> uploadProducts(List<ProductForm> forms) throws ApiException {
        // Convert forms to entities
        List<ProductEntity> entities = forms.stream()
            .map(form -> {
                try {
                    validateForm(form);
                    return convertFormToEntity(form);
                } catch (ApiException e) {
                    // Re-throw as runtime exception to be caught in the flow layer
                    throw new RuntimeException(e.getMessage(), e);
                }
            })
            .collect(java.util.stream.Collectors.toList());
        
        // Delegate to flow layer
        return flow.uploadProducts(entities, forms);
    }

    // Helper methods
    private void validateForm(ProductForm form) throws ApiException {
        if (form == null) {
            throw new ApiException("Product form cannot be null");
        }
        if (StringUtil.isEmpty(form.getName())) {
            throw new ApiException("Product name cannot be empty");
        }
        if (StringUtil.isEmpty(form.getBarcode())) {
            throw new ApiException("Product barcode cannot be empty");
        }
        // Add barcode length validation
        if (form.getBarcode().length() > MAX_BARCODE_LENGTH) {
            throw new ApiException("Barcode is too long. Maximum length allowed is " + MAX_BARCODE_LENGTH + " characters");
        }
        if (form.getClientId() == null) {
            throw new ApiException("Client ID cannot be null");
        }
        if (form.getMrp() == null || form.getMrp().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("MRP must be greater than 0");
        }
    }

    private ProductEntity convertFormToEntity(ProductForm form) {
        ProductEntity entity = new ProductEntity();
        entity.setName(form.getName());
        entity.setBarcode(form.getBarcode());
        entity.setMrp(form.getMrp());
        entity.setClientId(form.getClientId());
        return entity;
    }
} 