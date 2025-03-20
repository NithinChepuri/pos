package com.increff.dto;

import com.increff.model.ProductData;
import com.increff.model.ProductForm;
import com.increff.model.ProductUploadForm;
import com.increff.model.ProductSearchForm;
import com.increff.service.ApiException;
import com.increff.model.UploadResult;
import com.increff.util.TsvUtil;
import com.increff.flow.ProductFlow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class ProductDto {

    @Autowired
    private ProductFlow flow;

    public ProductData add(ProductForm form) throws ApiException {
        return flow.add(form);
    }

    public ProductData get(Long id) throws ApiException {
        return flow.get(id);
    }

    public List<ProductData> getAll(int page, int size) {
        return flow.getAll(page, size);
    }

    public ProductData update(Long id, ProductForm form) throws ApiException {
        return flow.update(id, form);
    }

    public List<ProductData> search(ProductSearchForm form, int page, int size) {
        return flow.search(form, page, size);
    }

    public void delete(Long id) throws ApiException {
        flow.delete(id);
    }



    public ResponseEntity<UploadResult<ProductData>> processUpload(MultipartFile file) {
        try {
            List<ProductForm> forms = TsvUtil.readProductsFromTsv(file);
            UploadResult<ProductData> result = flow.uploadProducts(forms);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return createErrorResponse("Error reading file: " + e.getMessage());
        } catch (ApiException e) {
            return createErrorResponse(e.getMessage());
        }
    }

    private ResponseEntity<UploadResult<ProductData>> createErrorResponse(String errorMessage) {
        UploadResult<ProductData> errorResult = new UploadResult<>();
        errorResult.addError(0, null, errorMessage);
        return ResponseEntity.badRequest().body(errorResult);
    }

    public ResponseEntity<ProductData> updateProduct(Long id, ProductForm form) {
        try {
            ProductData updatedProduct = update(id, form);
            return ResponseEntity.ok(updatedProduct);
        } catch (ApiException e) {
            // Create a ProductData object to hold the error message
            ProductData errorData = new ProductData();
            errorData.setName("Error: " + e.getMessage()); // Use a field to store the error message
            return ResponseEntity.badRequest().body(errorData);
        }
    }
} 