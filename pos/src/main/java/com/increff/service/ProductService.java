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


    @Transactional
    public void add(ProductEntity product) {
        dao.insert(product);
    }


    @Transactional
    public ProductEntity addProduct(ProductEntity product) {
        dao.insert(product);
        return product;
    }


    @Transactional(readOnly = true)
    public ProductEntity get(Long id) {
        return dao.select(id);
    }


    @Transactional(readOnly = true)
    public ProductEntity getChecked(Long id) throws ApiException {
        ProductEntity product = get(id);
        if (product == null) {
            throw new ApiException("Product not found with id: " + id);
        }
        return product;
    }


    @Transactional(readOnly = true)
    public List<ProductEntity> getAll(int page, int size) {
        return dao.selectAll(page, size);
    }


    @Transactional
    public void update(ProductEntity product) {
        dao.update(product);
    }


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


    @Transactional(readOnly = true)
    public ProductEntity getByBarcode(String barcode) {
        return dao.selectByBarcode(barcode);
    }


    @Transactional(readOnly = true)
    public List<ProductEntity> search(ProductSearchForm form, int page, int size) {
        return dao.search(form, page, size);
    }


    @Transactional
    public void deleteProduct(Long id) throws ApiException {
        if (id == null) {
            throw new ApiException("Product ID cannot be null");
        }

        ProductEntity product = get(id);
        if (product == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }

        
        if (inventoryService != null && inventoryService.existsByProductId(id)) {
            throw new ApiException("Cannot delete product that has inventory");
        }

        dao.delete(product);
    }
} 