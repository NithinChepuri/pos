package com.increff.dto;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.flow.ProductFlow;
import com.increff.service.ProductService;
import com.increff.util.ConversionUtil;
import com.increff.util.StringUtil;
import com.increff.util.TsvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.increff.model.Constants.MAX_BARCODE_LENGTH;

@Component
public class ProductDto {

    @Autowired
    private ProductFlow flow;

    @Autowired
    private ProductService service;

    public ProductData add(ProductForm form) throws ApiException {
        // Validate form
        validateForm(form);

        // Convert form to entity
        ProductEntity entity = ConversionUtil.convertProductFormToEntity(form);

        // Delegate to flow layer
        return flow.add(entity);
    }

    //TODO: have conversions in dto layer itself
    public ProductData get(Long id) throws ApiException {
        return service.getProductData(id);
    }

    public List<ProductData> getAll(int page, int size) {
        return service.getAllProductData(page, size);
    }

    public ProductData update(Long id, ProductForm form) throws ApiException {
        try {
            validateForm(form);
            ProductEntity entity = ConversionUtil.convertProductFormToEntity(form);
            return flow.update(id, entity);
        } catch (Exception e) {
            throw new ApiException("Error updating product: " + e.getMessage());
        }
    }

    public List<ProductData> search(ProductSearchForm form, int page, int size) {
        return service.searchProductData(form, page, size);
    }

    public void delete(Long id) throws ApiException {
        service.deleteProduct(id);
    }

    public UploadResult<ProductData> uploadProducts(List<ProductForm> forms) throws ApiException {
        // Convert forms to entities
        List<ProductEntity> entities = forms.stream()
            .map(form -> {
                try {
                    validateForm(form);
                    return ConversionUtil.convertProductFormToEntity(form);
                } catch (ApiException e) {
                    // Re-throw as runtime exception to be caught in the flow layer
                    throw new RuntimeException(e.getMessage(), e);
                }
            })
            .collect(java.util.stream.Collectors.toList());
        
        // Delegate to flow layer
        return flow.uploadProducts(entities, forms);
    }

    public UploadResult<ProductData> upload(MultipartFile file) throws ApiException {
        try {
            List<ProductForm> forms = TsvUtil.readProductsFromTsv(file);
            return uploadProducts(forms);
        } catch (IOException e) {
            UploadResult<ProductData> errorResult = new UploadResult<>();
            errorResult.addError(0, null, "Error reading file: " + e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    //TODO: remove the unnecessary validations
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

} 