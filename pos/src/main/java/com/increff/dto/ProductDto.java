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
import com.increff.util.ProductTsvUtil;
import com.increff.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.increff.model.Constants.MAX_BARCODE_LENGTH;
import static com.increff.util.ValidationUtil.validateName;

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
        //todo move to catch block
        UploadResult<ProductData> result = new UploadResult<>();
        
        try {
            ValidationUtil.validateProductUploadFile(file);
            List<ProductForm> forms = parseProductForms(file);
            return uploadProducts(forms);
        } catch (IOException e) {
            handleFileError(result, e);
            return result;
        }
    }
//    Parse product forms from TSV file
    private List<ProductForm> parseProductForms(MultipartFile file) throws IOException, ApiException {
        List<ProductForm> forms = ProductTsvUtil.readProductsFromTsv(file);
        
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
            //todo check if this is needed
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
        ValidationUtil.validateProductFormNotNull(form);
        validateBasicFields(form.getName(), form.getBarcode(), form.getClientId());
        ValidationUtil.validateMrp(form.getMrp());
    }

    private void validateBasicFields(String name, String barcode, Long clientId) throws ApiException {
        ValidationUtil.validateName(name);
        ValidationUtil.validateBarcode(barcode);
        ValidationUtil.validateClientId(clientId);
    }


    private void handleFileError(UploadResult<ProductData> result, IOException e) {
        result.addError(0, "File Error", "Error reading file: " + e.getMessage());
    }
} 
