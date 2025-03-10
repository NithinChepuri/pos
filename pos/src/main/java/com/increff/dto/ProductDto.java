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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class ProductDto {

    @Autowired
    private ProductService service;

    @Autowired
    private ClientService clientService;

    public ProductData add(ProductForm form) {
        ProductEntity product = convert(form);
        return convert(service.add(product));
    }

    public ProductData get(Long id) {
        return convert(service.get(id));
    }

    public List<ProductData> getAll() {
        List<ProductData> list = new ArrayList<>();
        for (ProductEntity product : service.getAll()) {
            list.add(convert(product));
        }
        return list;
    }

    public ProductData update(Long id, ProductForm form) throws ApiException {
        // Normalize the data
        normalize(form);
        // Call service with new signature
        service.update(id, form);
        // Return updated product
        return get(id);
    }

    private void normalize(ProductForm form) {
        if (form.getName() != null) {
            form.setName(form.getName().trim());
        }
        if (form.getBarcode() != null) {
            form.setBarcode(form.getBarcode().trim());
        }
    }

    public void delete(Long id) {
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

    private ProductEntity convert(ProductForm form) {
        ProductEntity product = new ProductEntity();
        product.setName(form.getName());
        product.setBarcode(form.getBarcode());
        product.setClientId(form.getClientId());
        product.setMrp(form.getMrp());
        return product;
    }

    private ProductData convert(ProductEntity product) {
        ProductData data = new ProductData();
        data.setId(product.getId());
        data.setName(product.getName());
        data.setBarcode(product.getBarcode());
        data.setClientId(product.getClientId());
        data.setMrp(product.getMrp());
        return data;
    }

    public List<ProductData> search(ProductForm form) {
        List<ProductEntity> products = service.search(form);
        return products.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public UploadResult<ProductData> uploadProducts(List<ProductForm> forms) throws ApiException {
        return service.uploadProducts(forms);
    }
} 