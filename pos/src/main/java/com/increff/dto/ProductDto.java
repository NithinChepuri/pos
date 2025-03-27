package com.increff.dto;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.ProductUpdateForm;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        ProductEntity entity = convertProductFormToEntity(form);

        // Delegate to flow layer
        ProductEntity addedEntity = flow.add(entity);
        
        // Convert entity to data and return
        return convertToProductData(addedEntity);
    }

    public ProductData get(Long id) throws ApiException {
        ProductEntity entity = service.getChecked(id);
        return convertToProductData(entity);
    }

    public List<ProductData> getAll(int page, int size) {
        List<ProductEntity> entities = service.getAll(page, size);
        return entities.stream()
                .map(this::convertToProductData)
                .collect(Collectors.toList());
    }

    public ProductData update(Long id, ProductUpdateForm form) throws ApiException {
        validateUpdateForm(form);
        
        // Convert update form to entity
        ProductEntity entity = new ProductEntity();
        entity.setName(form.getName());
        entity.setBarcode(form.getBarcode());
        entity.setClientId(form.getClientId());
        entity.setMrp(form.getMrp());
        
        ProductEntity updatedEntity = flow.update(id, entity);
        return convertToProductData(updatedEntity);
    }

    public List<ProductData> search(ProductSearchForm form, int page, int size) {
        List<ProductEntity> entities = service.search(form, page, size);
        return entities.stream()
                .map(this::convertToProductData)
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws ApiException {
        service.deleteProduct(id);
    }

    public UploadResult<ProductData> upload(MultipartFile file) throws ApiException {
        UploadResult<ProductData> result = new UploadResult<>();
        
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                result.addError(0, "File Error", "File is empty");
                return result;
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".tsv")) {
                result.addError(0, "File Error", "Only TSV files are supported");
                return result;
            }
            
            List<ProductForm> forms = TsvUtil.readProductsFromTsv(file);
            result.setTotalRows(forms.size());
            
            if (forms.isEmpty()) {
                result.addError(0, "Empty File", "No valid product data found in the file");
                return result;
            }
            
            return uploadProducts(forms);
        } catch (IOException e) {
            result.addError(0, "File Error", "Error reading file: " + e.getMessage());
            return result;
        }
    }

    public UploadResult<ProductData> uploadProducts(List<ProductForm> forms) throws ApiException {
        UploadResult<ProductData> result = new UploadResult<>();
        result.setTotalRows(forms.size());
        
        // Process each form individually to continue on errors
        for (int i = 0; i < forms.size(); i++) {
            ProductForm form = forms.get(i);
            try {
                // Validate form
                validateForm(form);
                
                // Convert form to entity
                ProductEntity entity = convertProductFormToEntity(form);
                
                // Check if product with barcode already exists
                ProductEntity existingProduct = service.getByBarcode(form.getBarcode());
                if (existingProduct != null) {
                    result.addError(i + 1, form, "Product with barcode " + form.getBarcode() + " already exists");
                    continue;
                }
                
                // Add product
                ProductEntity addedEntity = flow.add(entity);
                
                // Convert to data and add to successful entries
                ProductData data = convertToProductData(addedEntity);
                result.addSuccess(data);
                
            } catch (ApiException e) {
                // Catch only ApiException and add to errors
                result.addError(i + 1, form, e.getMessage());
            }
        }
        
        return result;
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
    
    private void validateUpdateForm(ProductUpdateForm form) throws ApiException {
        if (form == null) {
            throw new ApiException("Product update form cannot be null");
        }
        if (StringUtil.isEmpty(form.getName())) {
            throw new ApiException("Product name cannot be empty");
        }
        if (StringUtil.isEmpty(form.getBarcode())) {
            throw new ApiException("Product barcode cannot be empty");
        }
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
    
    // Conversion methods
    private ProductEntity convertProductFormToEntity(ProductForm form) {
        ProductEntity entity = new ProductEntity();
        entity.setName(form.getName());
        entity.setBarcode(form.getBarcode());
        entity.setMrp(form.getMrp());
        entity.setClientId(form.getClientId());
        return entity;
    }
    
    private ProductData convertToProductData(ProductEntity product) {
        ProductData data = new ProductData();
        data.setId(product.getId());
        data.setName(product.getName());
        data.setBarcode(product.getBarcode());
        data.setMrp(product.getMrp());
        data.setClientId(product.getClientId());
        return data;
    }
} 
