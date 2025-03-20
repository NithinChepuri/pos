package com.increff.flow;

import com.increff.entity.ProductEntity;
import com.increff.model.ProductData;
import com.increff.model.ProductForm;
import com.increff.model.ProductSearchForm;
import com.increff.model.ProductUploadForm;
import com.increff.model.UploadResult;
import com.increff.service.ApiException;
import com.increff.service.ClientService;
import com.increff.service.ProductService;
import com.increff.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductFlow {

    @Autowired
    private ProductService productService;

    @Autowired
    private ClientService clientService;

    public ProductData add(ProductForm form) throws ApiException {
        validateForm(form);
        
        // Check for duplicate barcode
        if (productService.getByBarcode(form.getBarcode()) != null) {
            throw new ApiException("Product with barcode " + form.getBarcode() + " already exists");
        }

        // Check if client exists
        if (!clientService.exists(form.getClientId())) {
            throw new ApiException("Client with ID " + form.getClientId() + " not found");
        }

        ProductEntity product = convert(form);
        productService.add(product);
        return convert(product);
    }

    public ProductData get(Long id) throws ApiException {
        ProductEntity product = productService.get(id);
        if (product == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        return convert(product);
    }

    public List<ProductData> getAll(int page, int size) {
        List<ProductEntity> products = productService.getAll(page, size);
        return products.stream().map(this::convert).collect(Collectors.toList());
    }

    public ProductData update(Long id, ProductForm form) throws ApiException {
        validateForm(form);
        
        ProductEntity existingProduct = productService.get(id);
        if (existingProduct == null) {
            throw new ApiException("Product not found with id: " + id);
        }

        // Check if barcode is being changed and if new barcode already exists
        if (!existingProduct.getBarcode().equals(form.getBarcode())) {
            ProductEntity productWithBarcode = productService.getByBarcode(form.getBarcode());
            if (productWithBarcode != null && !productWithBarcode.getId().equals(id)) {
                throw new ApiException("Product with barcode " + form.getBarcode() + " already exists");
            }
        }

        // Check if client exists
        if (!clientService.exists(form.getClientId())) {
            throw new ApiException("Client with ID " + form.getClientId() + " not found");
        }

        updateProduct(existingProduct, form);
        productService.update(existingProduct);
        return convert(existingProduct);
    }

    public List<ProductData> search(ProductSearchForm form, int page, int size) {
        List<ProductEntity> products = productService.search(form, page, size);
        return products.stream().map(this::convert).collect(Collectors.toList());
    }

    public void delete(Long id) throws ApiException {
        ProductEntity product = productService.get(id);
        if (product == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        productService.delete(id);
    }


    public UploadResult<ProductData> uploadProducts(List<ProductForm> forms) throws ApiException {
        UploadResult<ProductData> result = new UploadResult<>();
        int rowNumber = 0;

        for (ProductForm form : forms) {
            rowNumber++;
            try {
                validateForm(form);

                // Check for duplicate barcode
                if (productService.getByBarcode(form.getBarcode()) != null) {
                    throw new ApiException("Product with barcode " + form.getBarcode() + " already exists");
                }

                // Check if client exists
                if (!clientService.exists(form.getClientId())) {
                    throw new ApiException("Client with ID " + form.getClientId() + " not found");
                }

                ProductEntity product = convert(form);
                productService.add(product);
                result.getSuccessfulEntries().add(convert(product));
                result.setSuccessCount(result.getSuccessCount() + 1);

            } catch (Exception e) {
                result.addError(rowNumber, form, e.getMessage());
            }
        }

        result.setTotalRows(forms.size());
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
} 