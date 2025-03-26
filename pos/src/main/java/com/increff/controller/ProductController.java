package com.increff.controller;

import com.increff.dto.ProductDto;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import com.increff.util.AuthorizationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
        AuthorizationUtil.checkSupervisorAccess(request);
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
    public ProductData update(@PathVariable Long id, @RequestBody ProductForm form, HttpServletRequest request) throws ApiException {
        AuthorizationUtil.checkSupervisorAccess(request);
        return dto.update(id, form);
    }

    @ApiOperation(value = "Upload products from TSV file")
    @PostMapping("/upload")
    public UploadResult<ProductData> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws ApiException {
        AuthorizationUtil.checkSupervisorAccess(request);
        return dto.upload(file);
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
        AuthorizationUtil.checkSupervisorAccess(request);
        dto.delete(id);
    }
}
