package com.increff.service;

import com.increff.dao.ProductDao;
import com.increff.entity.ProductEntity;
import com.increff.model.ProductForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.increff.model.UploadResult;
import com.increff.model.ProductData;
import com.increff.service.ClientService;
import com.increff.util.StringUtil;
import com.increff.model.ProductSearchForm;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductDao dao;

    @Autowired
    private ClientService clientService;

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

    @Transactional(readOnly = true)
    public List<ProductEntity> search(ProductForm form) {
        ProductSearchForm searchForm = new ProductSearchForm();
        searchForm.setName(form.getName());
        searchForm.setBarcode(form.getBarcode());
        searchForm.setClientId(form.getClientId());
        searchForm.setClientName(form.getClientName());
        return dao.search(searchForm, 0, 10);
    }

    @Transactional
    public UploadResult<ProductData> uploadProducts(List<ProductForm> forms) {
        UploadResult<ProductData> result = new UploadResult<>();
        result.setTotalRows(forms.size());

        for (int i = 0; i < forms.size(); i++) {
            ProductForm form = forms.get(i);
            try {
                ProductEntity product = convert(form);
                dao.insert(product);
                result.addSuccess(convert(product));
            } catch (Exception e) {
                ProductForm errorForm = new ProductForm();
                errorForm.setBarcode(form.getBarcode());
                result.addError(i + 1, errorForm, "Internal error: " + e.getMessage());
            }
        }

        return result;
    }

    private ProductEntity convert(ProductForm form) {
        ProductEntity product = new ProductEntity();
        product.setClientId(form.getClientId());
        product.setName(form.getName());
        product.setBarcode(form.getBarcode());
        product.setMrp(form.getMrp());
        return product;
    }

    private ProductData convert(ProductEntity entity) {
        ProductData data = new ProductData();
        data.setId(entity.getId());
        data.setClientId(entity.getClientId());
        data.setName(entity.getName());
        data.setBarcode(entity.getBarcode());
        data.setMrp(entity.getMrp());
        return data;
    }

    @Transactional
    public void delete(Long id) {
        ProductEntity product = get(id);
        if (product != null) {
            dao.delete(product);
        }
    }
} 