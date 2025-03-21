package com.increff.service;

import com.increff.dao.ProductDao;
import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductSearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductDao dao;

    /**
     * Add a new product
     */
    @Transactional
    public void add(ProductEntity product) {
        dao.insert(product);
    }

    /**
     * Add a product and return its data representation
     */
    @Transactional
    public ProductData addProduct(ProductEntity product) {
        dao.insert(product);
        return convertToProductData(product);
    }

    /**
     * Get a product by ID
     */
    @Transactional(readOnly = true)
    public ProductEntity get(Long id) {
        return dao.select(id);
    }

    /**
     * Get a product by ID and return its data representation
     */
    @Transactional(readOnly = true)
    public ProductData getProductData(Long id) throws ApiException {
        ProductEntity product = get(id);
        if (product == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        return convertToProductData(product);
    }

    /**
     * Get all products with pagination
     */
    @Transactional(readOnly = true)
    public List<ProductEntity> getAll(int page, int size) {
        return dao.selectAll(page, size);
    }

    /**
     * Get all products with pagination and return their data representations
     */
    @Transactional(readOnly = true)
    public List<ProductData> getAllProductData(int page, int size) {
        List<ProductEntity> products = getAll(page, size);
        return products.stream()
                .map(this::convertToProductData)
                .collect(Collectors.toList());
    }

    /**
     * Update a product
     */
    @Transactional
    public void update(ProductEntity product) {
        dao.update(product);
    }

    /**
     * Update a product and return its data representation
     */
    @Transactional
    public ProductData updateProduct(ProductEntity existingProduct, ProductEntity updatedProduct) {
        // Update existing product with new values
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setBarcode(updatedProduct.getBarcode());
        existingProduct.setMrp(updatedProduct.getMrp());
        existingProduct.setClientId(updatedProduct.getClientId());
        
        update(existingProduct);
        return convertToProductData(existingProduct);
    }

    /**
     * Get a product by barcode
     */
    @Transactional(readOnly = true)
    public ProductEntity getByBarcode(String barcode) {
        return dao.selectByBarcode(barcode);
    }

    /**
     * Search products with pagination
     */
    @Transactional(readOnly = true)
    public List<ProductEntity> search(ProductSearchForm form, int page, int size) {
        return dao.search(form, page, size);
    }

    /**
     * Search products with pagination and return their data representations
     */
    @Transactional(readOnly = true)
    public List<ProductData> searchProductData(ProductSearchForm form, int page, int size) {
        List<ProductEntity> products = search(form, page, size);
        return products.stream()
                .map(this::convertToProductData)
                .collect(Collectors.toList());
    }

    /**
     * Delete a product
     */
    @Transactional
    public void delete(Long id) {
        ProductEntity product = get(id);
        if (product != null) {
            dao.delete(product);
        }
    }

    /**
     * Delete a product and handle not found case
     */
    @Transactional
    public void deleteProduct(Long id) throws ApiException {
        ProductEntity product = get(id);
        if (product == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        dao.delete(product);
    }

    /**
     * Convert a product entity to its data representation
     */
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