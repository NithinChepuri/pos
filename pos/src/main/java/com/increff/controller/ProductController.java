package com.increff.controller;

import com.increff.dto.ProductDto;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.util.TsvUtil;
import com.increff.model.enums.Role;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductDto dto;

    @ApiOperation(value = "Add a product")
    @PostMapping
    public ProductData add(@RequestBody ProductForm form, HttpServletRequest request) throws ApiException {
        checkSupervisorAccess(request);
        return dto.add(form);
    }

    @ApiOperation(value = "Get a product by ID")
    @GetMapping("/{id}")
    public ProductData get(@PathVariable Long id) throws ApiException {
        return dto.get(id);
    }

    @ApiOperation(value = "Get all products with pagination")
    @GetMapping
    public List<ProductData> getAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return dto.getAll(page, size);
    }

    @ApiOperation(value = "Update a product")
    @PutMapping("/{id}")
    public ResponseEntity<ProductData> update(@PathVariable Long id, @RequestBody ProductForm form, HttpServletRequest request) {
        try {
            checkSupervisorAccess(request);
            ProductData updatedProduct = dto.update(id, form);
            return ResponseEntity.ok(updatedProduct);
        } catch (ApiException e) {
            // Create a ProductData object to hold the error message
            ProductData errorData = new ProductData();
            errorData.setName("Error: " + e.getMessage()); // Use a field to store the error message
            return ResponseEntity.badRequest().body(errorData);
        }
    }

    @ApiOperation(value = "Upload products from TSV file")
    @PostMapping("/upload")
    public ResponseEntity<UploadResult<ProductData>> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            checkSupervisorAccess(request);
            List<ProductForm> forms = TsvUtil.readProductsFromTsv(file);
            UploadResult<ProductData> result = dto.uploadProducts(forms);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return createErrorResponse("Error reading file: " + e.getMessage());
        } catch (ApiException e) {
            return createErrorResponse(e.getMessage());
        }
    }

    private ResponseEntity<UploadResult<ProductData>> createErrorResponse(String errorMessage) {
        UploadResult<ProductData> errorResult = new UploadResult<>();
        errorResult.addError(0, null, errorMessage);
        return ResponseEntity.badRequest().body(errorResult);
    }

    @ApiOperation(value = "Search products")
    @PostMapping("/search")
    public List<ProductData> search(
            @RequestBody ProductSearchForm form,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return dto.search(form, page, size);
    }

    @ApiOperation(value = "Delete a product")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) throws ApiException {
        checkSupervisorAccess(request);
        dto.delete(id);
    }
    
    /**
     * Check if the current user has supervisor access
     */
    private void checkSupervisorAccess(HttpServletRequest request) throws ApiException {
        HttpSession session = request.getSession();
        Role role = (Role) session.getAttribute("role");
        
        if (role != Role.SUPERVISOR) {
            throw new ApiException("Access denied. Supervisor role required.");
        }
    }
}
