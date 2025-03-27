package com.increff.service;

import com.increff.dao.ProductDao;
import com.increff.entity.ProductEntity;
import com.increff.entity.InventoryEntity;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductSearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductDao dao;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderItemService orderItemService;

    /**
     * Add a new product
     */
    @Transactional
    public void add(ProductEntity product) {
        dao.insert(product);
    }

    /**
     * Add a product and return the entity
     */
    @Transactional
    public ProductEntity addProduct(ProductEntity product) {
        dao.insert(product);
        return product;
    }

    /**
     * Get a product by ID
     */
    @Transactional(readOnly = true)
    public ProductEntity get(Long id) {
        return dao.select(id);
    }

    /**
     * Get a product by ID and check if it exists
     */
    @Transactional(readOnly = true)
    public ProductEntity getChecked(Long id) throws ApiException {
        ProductEntity product = get(id);
        if (product == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        return product;
    }

    /**
     * Get all products with pagination
     */
    @Transactional(readOnly = true)
    public List<ProductEntity> getAll(int page, int size) {
        return dao.selectAll(page, size);
    }

    /**
     * Update a product
     */
    @Transactional
    public void update(ProductEntity product) {
        dao.update(product);
    }

    /**
     * Update a product and return the updated entity
     */
    @Transactional
    public ProductEntity updateProduct(ProductEntity existingProduct, ProductEntity updatedProduct) {
        // Update existing product with new values
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setBarcode(updatedProduct.getBarcode());
        existingProduct.setMrp(updatedProduct.getMrp());
        existingProduct.setClientId(updatedProduct.getClientId());
        
        update(existingProduct);
        return existingProduct;
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
     * Force delete a product and its dependencies
     */
    @Transactional
    public void deleteProduct(Long id) throws ApiException {
        // Add null check
        if (id == null) {
            throw new ApiException("Product ID cannot be null");
        }
        
        // Get the product and check if it exists
        ProductEntity product = get(id);
        if (product == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }
        
        // Check if product is used in orders or inventory before deletion
        if (orderItemService != null && orderItemService.existsByProductId(id)) {
            throw new ApiException("Cannot delete product that is used in orders");
        }
        
        if (inventoryService != null && inventoryService.existsByProductId(id)) {
            throw new ApiException("Cannot delete product that has inventory");
        }
        
        // Delete the product
        dao.delete(product);
    }
} 