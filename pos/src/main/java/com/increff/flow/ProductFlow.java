package com.increff.flow;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.service.ClientService;
import com.increff.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductFlow {

    private static final Logger logger = LoggerFactory.getLogger(ProductFlow.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ClientService clientService;



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
     * Upload multiple products
     */
    public UploadResult<ProductData> uploadProducts(List<ProductEntity> products, List<ProductForm> originalForms) {
        UploadResult<ProductData> result = new UploadResult<>();
        
        for (int i = 0; i < products.size(); i++) {
            ProductEntity product = products.get(i);
            ProductForm originalForm = originalForms.get(i);
            
            try {
                // Log the product being processed
                logger.info("Processing product: {}", product.getBarcode());
                
                // Business validation
                validateProductDoesNotExist(product.getBarcode());
                validateClientExists(product.getClientId());
                
                // Add product
                ProductData productData = productService.addProduct(product);
                result.getSuccessfulEntries().add(productData);
                result.setSuccessCount(result.getSuccessCount() + 1);
                
                logger.info("Successfully added product: {}", product.getBarcode());
            } catch (Exception e) {
                logger.error("Error adding product: {} - {}", product.getBarcode(), e.getMessage(), e);
                result.addError(i + 1, originalForm, e.getMessage());
            }
        }
        
        return result;
    }

//    Validate that a product with the given barcode doesn't already exist
    private void validateProductDoesNotExist(String barcode) throws ApiException {
        if (productService.getByBarcode(barcode) != null) {
            throw new ApiException("Product with barcode " + barcode + " already exists");
        }
    }

//  Validate that the client exists
    private void validateClientExists(Long clientId) throws ApiException {
        if (!clientService.exists(clientId)) {
            throw new ApiException("Client with ID " + clientId + " not found");
        }
    }
    

//  Validate that a barcode change doesn't conflict with an existing product
    private void validateBarcodeChangeIsUnique(ProductEntity existingProduct, ProductEntity updatedProduct) throws ApiException {
        if (!existingProduct.getBarcode().equals(updatedProduct.getBarcode())) {
            ProductEntity productWithBarcode = productService.getByBarcode(updatedProduct.getBarcode());
            if (productWithBarcode != null && !productWithBarcode.getId().equals(existingProduct.getId())) {
                throw new ApiException("Product with barcode " + updatedProduct.getBarcode() + " already exists");
            }
        }
    }
} 