package com.increff.dto;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.ProductUpdateForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.flow.ProductFlow;
import com.increff.service.ProductService;
import com.increff.util.ConversionUtil;
import com.increff.util.StringUtil;
import com.increff.util.TsvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.increff.model.Constants.MAX_BARCODE_LENGTH;

@Component
public class ProductDto {

    @Autowired
    private ProductFlow flow;

    @Autowired
    private ProductService service;

    public ProductData add(ProductForm form) throws ApiException {
        ProductEntity entity = ConversionUtil.convertProductFormToEntity(form);
        ProductEntity addedEntity = flow.add(entity);
        return ConversionUtil.convertProductEntityToData(addedEntity);
    }

    public ProductData get(Long id) throws ApiException {
        ProductEntity entity = service.getChecked(id);
        return ConversionUtil.convertProductEntityToData(entity);
    }

    public List<ProductData> getAll(int page, int size) {
        List<ProductEntity> entities = service.getAll(page, size);
        return entities.stream()
                .map(ConversionUtil::convertProductEntityToData)
                .collect(Collectors.toList());
    }

    public ProductData update(Long id, ProductUpdateForm form) throws ApiException {
        ProductEntity entity = ConversionUtil.convertUpdateFormToEntity(form);
        ProductEntity updatedEntity = flow.update(id, entity);
        return ConversionUtil.convertProductEntityToData(updatedEntity);
    }

    public List<ProductData> search(ProductSearchForm form, int page, int size) {
        List<ProductEntity> entities = service.search(form, page, size);
        return entities.stream()
                .map(ConversionUtil::convertProductEntityToData)
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws ApiException {
        service.deleteProduct(id);
    }

    public UploadResult<ProductData> upload(MultipartFile file) throws ApiException {
        UploadResult<ProductData> result = new UploadResult<>();
        
        try {
            validateUploadFile(file);
            List<ProductForm> forms = parseProductForms(file);
            return uploadProducts(forms);
        } catch (IOException e) {
            handleFileError(result, e);
            return result;
        }
    }

    private void validateUploadFile(MultipartFile file) throws ApiException {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File is empty");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".tsv")) {
            throw new ApiException("Only TSV files are supported");
        }
    }

//    Parse product forms from TSV file
    private List<ProductForm> parseProductForms(MultipartFile file) throws IOException, ApiException {
        List<ProductForm> forms = TsvUtil.readProductsFromTsv(file);
        
        if (forms.isEmpty()) {
            throw new ApiException("No valid product data found in the file");
        }
        
        return forms;
    }



    public UploadResult<ProductData> uploadProducts( List<ProductForm> forms) throws ApiException {
        UploadResult<ProductData> result = initializeUploadResult(forms.size());
        
        for (int i = 0; i < forms.size(); i++) {
            processProductForm(forms.get(i), i + 1, result);
        }
        
        return result;
    }

    /**
     * Initialize upload result with total rows
     */
    private UploadResult<ProductData> initializeUploadResult(int totalRows) {
        UploadResult<ProductData> result = new UploadResult<>();
        result.setTotalRows(totalRows);
        return result;
    }

    /**
     * Process a single product form during upload
     */
    private void processProductForm(ProductForm form, int lineNumber, UploadResult<ProductData> result) {
        try {
            validateForm(form);
            ProductEntity entity = ConversionUtil.convertProductFormToEntity(form);
            
            if (isProductExisting(form.getBarcode())) {
                result.addError(lineNumber, form, 
                    "Product with barcode " + form.getBarcode() + " already exists");
                return;
            }
            
            ProductData data = addProductAndCreateData(entity);
            result.addSuccess(data);
            
        } catch (ApiException e) {
            result.addError(lineNumber, form, e.getMessage());
        }
    }

    private boolean isProductExisting(String barcode) {
        return service.getByBarcode(barcode) != null;
    }

    private ProductData addProductAndCreateData(ProductEntity entity) throws ApiException {
        ProductEntity addedEntity = flow.add(entity);
        return ConversionUtil.convertProductEntityToData(addedEntity);
    }

    // Helper methods
    private void validateForm(ProductForm form) throws ApiException {
        validateFormNotNull(form);
        validateBasicFields(form.getName(), form.getBarcode(), form.getClientId());
        validateMrp(form.getMrp());
    }


    private void validateFormNotNull(Object form) throws ApiException {
        if (form == null) {
            throw new ApiException("Product form cannot be null");
        }
    }

    private void validateBasicFields(String name, String barcode, Long clientId) throws ApiException {
        validateName(name);
        validateBarcode(barcode);
        validateClientId(clientId);
    }

    private void validateName(String name) throws ApiException {
        if (StringUtil.isEmpty(name)) {
            throw new ApiException("Product name cannot be empty");
        }
    }
    private void validateBarcode(String barcode) throws ApiException {
        if (StringUtil.isEmpty(barcode)) {
            throw new ApiException("Product barcode cannot be empty");
        }
        if (barcode.length() > MAX_BARCODE_LENGTH) {
            throw new ApiException("Barcode is too long. Maximum length allowed is " + MAX_BARCODE_LENGTH + " characters");
        }
    }
    private void validateClientId(Long clientId) throws ApiException {
        if (clientId == null) {
            throw new ApiException("Client ID cannot be null");
        }
    }

    private void validateMrp(BigDecimal mrp) throws ApiException {
        if (mrp == null || mrp.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("MRP must be greater than 0");
        }
    }

    private void handleFileError(UploadResult<ProductData> result, IOException e) {
        result.addError(0, "File Error", "Error reading file: " + e.getMessage());
    }
} 
