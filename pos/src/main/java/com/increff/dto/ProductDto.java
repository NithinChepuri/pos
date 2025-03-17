package com.increff.dto;

import com.increff.model.ProductData;
import com.increff.model.ProductForm;
import com.increff.model.ProductUploadForm;
import com.increff.entity.ProductEntity;
import com.increff.entity.ClientEntity;
import com.increff.service.ProductService;
import com.increff.service.ClientService;
import com.increff.service.ApiException;
import com.increff.util.StringUtil;
import com.increff.model.UploadResult;
import com.increff.model.UploadError;
import com.increff.util.TsvUtil;
import com.increff.model.ProductSearchForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Component
public class ProductDto {

    @Autowired
    private ProductService service;

    @Autowired
    private ClientService clientService;

    public ProductData add(ProductForm form) throws ApiException {
        validateForm(form);
        
        // Check for duplicate barcode
        if (service.getByBarcode(form.getBarcode()) != null) {
            throw new ApiException("Product with barcode " + form.getBarcode() + " already exists");
        }

        // Check if client exists
        if (!clientService.exists(form.getClientId())) {
            throw new ApiException("Client with ID " + form.getClientId() + " not found");
        }

        ProductEntity product = convert(form);
        service.add(product);
        return convert(product);
    }

    public ProductData get(Long id) throws ApiException {
        ProductEntity product = service.get(id);
        if (product == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        return convert(product);
    }

    public List<ProductData> getAll(int page, int size) {
        List<ProductEntity> products = service.getAll(page, size);
        return products.stream().map(this::convert).collect(Collectors.toList());
    }

    public ProductData update(Long id, ProductForm form) throws ApiException {
        validateForm(form);
        
        ProductEntity existingProduct = service.get(id);
        if (existingProduct == null) {
            throw new ApiException("Product not found with id: " + id);
        }

        // Check if barcode is being changed and if new barcode already exists
        if (!existingProduct.getBarcode().equals(form.getBarcode())) {
            ProductEntity productWithBarcode = service.getByBarcode(form.getBarcode());
            if (productWithBarcode != null && !productWithBarcode.getId().equals(id)) {
                throw new ApiException("Product with barcode " + form.getBarcode() + " already exists");
            }
        }

        // Check if client exists
        if (!clientService.exists(form.getClientId())) {
            throw new ApiException("Client with ID " + form.getClientId() + " not found");
        }

        updateProduct(existingProduct, form);
        service.update(existingProduct);
        return convert(existingProduct);
    }

    public List<ProductData> search(ProductSearchForm form, int page, int size) {
        List<ProductEntity> products = service.search(form, page, size);
        return products.stream().map(this::convert).collect(Collectors.toList());
    }

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
        if (form.getClientId() == null) {
            throw new ApiException("Client ID cannot be null");
        }
        if (form.getMrp() == null || form.getMrp().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("MRP must be greater than 0");
        }
    }

    private ProductData convert(ProductEntity product) {
        ProductData data = new ProductData();
        data.setId(product.getId());
        data.setName(product.getName());
        data.setBarcode(product.getBarcode());
        data.setMrp(product.getMrp());
        data.setClientId(product.getClientId());
        return data;
    }

    private ProductEntity convert(ProductForm form) {
        ProductEntity product = new ProductEntity();
        updateProduct(product, form);
        return product;
    }

    private void updateProduct(ProductEntity product, ProductForm form) {
        product.setName(form.getName());
        product.setBarcode(form.getBarcode());
        product.setMrp(form.getMrp());
        product.setClientId(form.getClientId());
    }

    public void delete(Long id) throws ApiException {
        ProductEntity product = service.get(id);
        if (product == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        service.delete(id);
    }

    public void bulkAdd(List<ProductUploadForm> uploadForms) throws ApiException {
        List<String> errors = new ArrayList<>();
        Set<String> barcodes = new HashSet<>();
        int lineNumber = 1;
        
        // First pass: Validate all rows and collect unique barcodes
        for (ProductUploadForm form : uploadForms) {
            lineNumber++;
            try {
                validateUploadForm(form, lineNumber);
                
                // Check for duplicate barcodes within the file
                if (!barcodes.add(form.getBarcode().trim().toLowerCase())) {
                    throw new ApiException("Duplicate barcode in file: " + form.getBarcode());
                }
                
            } catch (Exception e) {
                errors.add("Error at line " + lineNumber + ": " + e.getMessage());
            }
        }

        // If there are any validation errors, throw exception
        if (!errors.isEmpty()) {
            throw new ApiException("Validation errors in TSV file:\n" + String.join("\n", errors));
        }

        // Second pass: Add all products (only if all validations pass)
        for (ProductUploadForm form : uploadForms) {
            try {
                ProductForm productForm = convertUploadFormToProductForm(form);
                add(productForm);
            } catch (Exception e) {
                // This shouldn't happen as we've validated everything, but just in case
                throw new ApiException("Error while adding products: " + e.getMessage());
            }
        }
    }

    private void validateUploadForm(ProductUploadForm form, int lineNumber) throws ApiException {
        // Check for empty or null values
        if (form.getClientId() == null) {
            throw new ApiException("Client ID cannot be empty");
        }
        if (StringUtil.isEmpty(form.getName())) {
            throw new ApiException("Product name cannot be empty");
        }
        if (StringUtil.isEmpty(form.getBarcode())) {
            throw new ApiException("Barcode cannot be empty");
        }
        if (StringUtil.isEmpty(form.getMrp())) {
            throw new ApiException("MRP cannot be empty");
        }

        // Validate MRP format and value
        BigDecimal mrp;
        try {
            mrp = new BigDecimal(form.getMrp());
            if (mrp.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("MRP must be positive");
            }
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid MRP format");
        }

        // Check if client exists
        ClientEntity client = clientService.get(form.getClientId());
        if (client == null) {
            throw new ApiException("Client with ID " + form.getClientId() + " not found");
        }

        // Check if barcode already exists in database
        try {
            if (service.getByBarcode(form.getBarcode().trim()) != null) {
                throw new ApiException("Product with barcode " + form.getBarcode() + " already exists in database");
            }
        } catch (ApiException e) {
            // If getByBarcode throws ApiException, it means barcode doesn't exist, which is what we want
        }
    }

    private ProductForm convertUploadFormToProductForm(ProductUploadForm form) throws ApiException {
        ProductForm productForm = new ProductForm();
        productForm.setName(form.getName().trim());
        productForm.setBarcode(form.getBarcode().trim());
        productForm.setClientId(form.getClientId());
        productForm.setMrp(new BigDecimal(form.getMrp().trim()));
        return productForm;
    }

    public ResponseEntity<UploadResult<ProductData>> processUpload(MultipartFile file) {
        try {
            List<ProductForm> forms = TsvUtil.readProductsFromTsv(file);
            UploadResult<ProductData> result = uploadProducts(forms);
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

    public UploadResult<ProductData> uploadProducts(List<ProductForm> forms) throws ApiException {
        UploadResult<ProductData> result = new UploadResult<>();
        int rowNumber = 0;

        for (ProductForm form : forms) {
            rowNumber++;
            try {
                validateForm(form);

                // Check for duplicate barcode
                if (service.getByBarcode(form.getBarcode()) != null) {
                    throw new ApiException("Product with barcode " + form.getBarcode() + " already exists");
                }

                // Check if client exists
                if (!clientService.exists(form.getClientId())) {
                    throw new ApiException("Client with ID " + form.getClientId() + " not found");
                }

                ProductEntity product = convert(form);
                service.add(product);
                result.getSuccessfulEntries().add(convert(product));
                result.setSuccessCount(result.getSuccessCount() + 1);

            } catch (Exception e) {
                result.addError(rowNumber, form, e.getMessage());
            }
        }

        result.setTotalRows(forms.size());
        return result;
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