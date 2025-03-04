package com.increff.service;

import com.increff.dao.ProductDao;
import com.increff.dao.ClientDao;
import com.increff.entity.ProductEntity;
import com.increff.entity.ClientEntity;
import com.increff.model.ProductForm;
import com.increff.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.increff.model.UploadResult;
import com.increff.model.ProductData;
import com.increff.service.ClientService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductDao dao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private ClientService clientService;

    public ProductEntity add(ProductEntity product) throws ApiException {
        validateProduct(product);
        dao.insert(product);
        return product;
    }

    @Transactional(readOnly = true)
    public ProductEntity get(Long id) throws ApiException {
        ProductEntity product = dao.select(id);
        if (product == null) {
            throw new ApiException("Product with id " + id + " not found");
        }
        return product;
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> getAll() {
        return dao.selectAll();
    }

    @Transactional
    public ProductEntity update(ProductEntity product) throws ApiException {
        validateProduct(product);
        ProductEntity existing = get(product.getId());
        existing.setName(product.getName());
        existing.setBarcode(product.getBarcode());
        existing.setClientId(product.getClientId());
        existing.setMrp(product.getMrp());
        return dao.update(existing);
    }

    @Transactional
    public void delete(Long id) throws ApiException {
        ProductEntity product = get(id);
        dao.delete(product);
    }

    @Transactional(readOnly = true)
    public ProductEntity getByBarcode(String barcode) throws ApiException {
        return dao.selectByBarcode(barcode);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> search(ProductForm form) {
        return dao.search(form);
    }

    @Transactional
    public UploadResult<ProductData> uploadProducts(List<ProductForm> forms) {
        UploadResult<ProductData> result = new UploadResult<>();
        result.setTotalRows(forms.size());

        for (int i = 0; i < forms.size(); i++) {
            ProductForm form = forms.get(i);
            try {
                // Validate entry
                validateProduct(form);
                
                // Process valid entry
                ProductEntity product = convert(form);
                dao.insert(product);
                result.addSuccess(convert(product));

            } catch (ApiException e) {
                result.addError(i + 1, form.getBarcode(), e.getMessage());
            } catch (Exception e) {
                result.addError(i + 1, form.getBarcode(), "Internal error: " + e.getMessage());
            }
        }

        return result;
    }

    private void validateProduct(ProductForm form) throws ApiException {
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        }
        if (form.getBarcode() == null || form.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode cannot be empty");
        }
        if (form.getMrp() == null || form.getMrp().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Product MRP must be positive");
        }
        
        // Validate client exists
        if (!clientService.exists(form.getClientId())) {
            throw new ApiException("Client with ID " + form.getClientId() + " not found");
        }

        // Check for duplicate barcode
        ProductEntity existing = dao.selectByBarcode(form.getBarcode());
        if (existing != null) {
            throw new ApiException("Product with barcode " + form.getBarcode() + " already exists");
        }
    }

    private void validateProduct(ProductEntity product) throws ApiException {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        }
        if (product.getBarcode() == null || product.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode cannot be empty");
        }
        if (product.getMrp() == null || product.getMrp().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Product MRP must be positive");
        }
        
        // Validate client exists
        ClientEntity client = clientDao.select(product.getClientId());
        if (client == null) {
            throw new ApiException("Client with ID " + product.getClientId() + " not found");
        }

        // Check for duplicate barcode (except for updates)
        ProductEntity existing = dao.selectByBarcode(product.getBarcode());
        if (existing != null && !existing.getId().equals(product.getId())) {
            throw new ApiException("Product with barcode " + product.getBarcode() + " already exists");
        }
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
} 