package com.increff.flow;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.service.ClientService;
import com.increff.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductFlow {

    @Autowired
    private ProductService productService;

    @Autowired
    private ClientService clientService;

    public ProductEntity add(ProductEntity product) throws ApiException {
        validateProductDoesNotExist(product.getBarcode());
        validateClientExists(product.getClientId());
        return productService.addProduct(product);
    }

    public ProductEntity update(Long id, ProductEntity updatedProduct) throws ApiException {
        ProductEntity existingProduct = productService.get(id);
        if (existingProduct == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        validateBarcodeChangeIsUnique(existingProduct, updatedProduct);
        validateClientExists(updatedProduct.getClientId());
        return productService.updateProduct(existingProduct, updatedProduct);
    }

    public UploadResult<ProductEntity> uploadProducts(List<ProductEntity> products, List<ProductForm> originalForms) {
        UploadResult<ProductEntity> result = new UploadResult<>();
        
        for (int i = 0; i < products.size(); i++) {
            ProductEntity product = products.get(i);
            ProductForm originalForm = originalForms.get(i);
            
            try {
                validateProductDoesNotExist(product.getBarcode());
                validateClientExists(product.getClientId());
                ProductEntity addedProduct = productService.addProduct(product);
                result.getSuccessfulEntries().add(addedProduct);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (ApiException e) {
                result.addError(i + 1, originalForm, e.getMessage());
            }
        }
        
        return result;
    }

    // Validate that a product with the given barcode doesn't already exist
    private void validateProductDoesNotExist(String barcode) throws ApiException {
        if (productService.getByBarcode(barcode) != null) {
            throw new ApiException("Product with barcode " + barcode + " already exists");
        }
    }

    // Validate that the client exists
    private void validateClientExists(Long clientId) throws ApiException {
        if (!clientService.exists(clientId)) {
            throw new ApiException("Client with ID " + clientId + " not found");
        }
    }
    
    // Validate that a barcode change doesn't conflict with an existing product
    private void validateBarcodeChangeIsUnique(ProductEntity existingProduct, ProductEntity updatedProduct) throws ApiException {
        if (!existingProduct.getBarcode().equals(updatedProduct.getBarcode())) {
            ProductEntity productWithBarcode = productService.getByBarcode(updatedProduct.getBarcode());
            if (productWithBarcode != null && !productWithBarcode.getId().equals(existingProduct.getId())) {
                throw new ApiException("Product with barcode " + updatedProduct.getBarcode() + " already exists");
            }
        }
    }
} 