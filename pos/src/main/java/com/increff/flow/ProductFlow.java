package com.increff.flow;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.service.ClientService;
import com.increff.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductFlow {

    @Autowired
    private ProductService productService;

    @Autowired
    private ClientService clientService;
    
    // Maximum allowed barcode length
    private static final int MAX_BARCODE_LENGTH = 50;

    /**
     * Add a new product after validating business rules
     */
    public ProductData add(ProductEntity product) throws ApiException {
        // Business validation
        validateProductDoesNotExist(product.getBarcode());
        validateClientExists(product.getClientId());

        // Delegate to service for persistence
        return productService.addProduct(product);
    }

    /**
     * Get product by ID
     */
    public ProductData get(Long id) throws ApiException {
        return productService.getProductData(id);
    }

    /**
     * Get all products with pagination
     */
    public List<ProductData> getAll(int page, int size) {
        return productService.getAllProductData(page, size);
    }

    /**
     * Update a product after validating business rules
     */
    public ProductData update(Long id, ProductEntity updatedProduct) throws ApiException {
        // Get existing product
        ProductEntity existingProduct = productService.get(id);
        if (existingProduct == null) {
            throw new ApiException("Product not found with id: " + id);
        }

        // Business validation
        validateBarcodeChangeIsUnique(existingProduct, updatedProduct);
        validateClientExists(updatedProduct.getClientId());

        // Update product
        return productService.updateProduct(existingProduct, updatedProduct);
    }

    /**
     * Search products
     */
    public List<ProductData> search(ProductSearchForm form, int page, int size) {
        return productService.searchProductData(form, page, size);
    }

    /**
     * Delete a product
     */
    public void delete(Long id) throws ApiException {
        productService.deleteProduct(id);
    }

    /**
     * Upload multiple products
     */
    public UploadResult<ProductData> uploadProducts(List<ProductEntity> products, List<ProductForm> originalForms) throws ApiException {
        UploadResult<ProductData> result = new UploadResult<>();
        result.setTotalRows(products.size());
        
        for (int i = 0; i < products.size(); i++) {
            processProductUpload(i, products.get(i), originalForms.get(i), result);
        }
        
        return result;
    }
    
    /**
     * Process a single product upload
     */
    private void processProductUpload(int index, ProductEntity product, ProductForm originalForm, UploadResult<ProductData> result) {
        try {
            // Business validation
            validateProductDoesNotExist(product.getBarcode());
            validateClientExists(product.getClientId());
            
            // Add product
            ProductData productData = productService.addProduct(product);
            result.getSuccessfulEntries().add(productData);
            result.setSuccessCount(result.getSuccessCount() + 1);
        } catch (Exception e) {
            result.addError(index + 1, originalForm, e.getMessage());
        }
    }
    
    /**
     * Validate that a product with the given barcode doesn't already exist
     */
    private void validateProductDoesNotExist(String barcode) throws ApiException {
        if (productService.getByBarcode(barcode) != null) {
            throw new ApiException("Product with barcode " + barcode + " already exists");
        }
    }
    
    /**
     * Validate that the client exists
     */
    private void validateClientExists(Long clientId) throws ApiException {
        if (!clientService.exists(clientId)) {
            throw new ApiException("Client with ID " + clientId + " not found");
        }
    }
    
    /**
     * Validate that a barcode change doesn't conflict with an existing product
     */
    private void validateBarcodeChangeIsUnique(ProductEntity existingProduct, ProductEntity updatedProduct) throws ApiException {
        if (!existingProduct.getBarcode().equals(updatedProduct.getBarcode())) {
            ProductEntity productWithBarcode = productService.getByBarcode(updatedProduct.getBarcode());
            if (productWithBarcode != null && !productWithBarcode.getId().equals(existingProduct.getId())) {
                throw new ApiException("Product with barcode " + updatedProduct.getBarcode() + " already exists");
            }
        }
    }
} 