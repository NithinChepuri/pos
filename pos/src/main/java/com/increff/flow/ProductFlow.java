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

    public ProductData add(ProductEntity product) throws ApiException {
        // Check for duplicate barcode
        if (productService.getByBarcode(product.getBarcode()) != null) {
            throw new ApiException("Product with barcode " + product.getBarcode() + " already exists");
        }

        // Check if client exists
        if (!clientService.exists(product.getClientId())) {
            throw new ApiException("Client with ID " + product.getClientId() + " not found");
        }

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

    public ProductData update(Long id, ProductEntity updatedProduct) throws ApiException {
        ProductEntity existingProduct = productService.get(id);
        if (existingProduct == null) {
            throw new ApiException("Product not found with id: " + id);
        }

        // Check if barcode is being changed and if new barcode already exists
        if (!existingProduct.getBarcode().equals(updatedProduct.getBarcode())) {
            ProductEntity productWithBarcode = productService.getByBarcode(updatedProduct.getBarcode());
            if (productWithBarcode != null && !productWithBarcode.getId().equals(id)) {
                throw new ApiException("Product with barcode " + updatedProduct.getBarcode() + " already exists");
            }
        }

        // Check if client exists
        if (!clientService.exists(updatedProduct.getClientId())) {
            throw new ApiException("Client with ID " + updatedProduct.getClientId() + " not found");
        }

        // Update existing product with new values
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setBarcode(updatedProduct.getBarcode());
        existingProduct.setMrp(updatedProduct.getMrp());
        existingProduct.setClientId(updatedProduct.getClientId());
        
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

    public UploadResult<ProductData> uploadProducts(List<ProductEntity> products, List<ProductForm> originalForms) throws ApiException {
        UploadResult<ProductData> result = new UploadResult<>();
        int rowNumber = 0;

        for (int i = 0; i < products.size(); i++) {
            rowNumber++;
            ProductEntity product = products.get(i);
            ProductForm originalForm = originalForms.get(i);
            
            try {
                // Check for duplicate barcode
                if (productService.getByBarcode(product.getBarcode()) != null) {
                    throw new ApiException("Product with barcode " + product.getBarcode() + " already exists");
                }

                // Check if client exists
                if (!clientService.exists(product.getClientId())) {
                    throw new ApiException("Client with ID " + product.getClientId() + " not found");
                }

                productService.add(product);
                result.getSuccessfulEntries().add(convert(product));
                result.setSuccessCount(result.getSuccessCount() + 1);

            } catch (Exception e) {
                result.addError(rowNumber, originalForm, e.getMessage());
            }
        }

        result.setTotalRows(products.size());
        return result;
    }

    // Helper methods
    private ProductData convert(ProductEntity product) {
        ProductData data = new ProductData();
        data.setId(product.getId());
        data.setName(product.getName());
        data.setBarcode(product.getBarcode());
        data.setMrp(product.getMrp());
        data.setClientId(product.getClientId());
        return data;
    }
} 