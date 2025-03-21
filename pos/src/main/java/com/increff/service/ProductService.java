package com.increff.service;

import com.increff.dao.ProductDao;
import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductSearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductDao dao;

    @Transactional
    public void add(ProductEntity product) {
        dao.insert(product);
    }

    @Transactional(readOnly = true)
    public ProductEntity get(Long id) {
        return dao.select(id);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> getAll(int page, int size) {
        return dao.selectAll(page, size);
    }

    @Transactional
    public void update(ProductEntity product) {
        dao.update(product);
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
    public void delete(Long id) {
        ProductEntity product = get(id);
        if (product != null) {
            dao.delete(product);
        }
    }
} 