package com.increff.controller;

import com.increff.dto.ProductDto;
import com.increff.model.products.ProductData;
import com.increff.model.products.ProductForm;
import com.increff.model.products.ProductSearchForm;
import com.increff.model.products.ProductUpdateForm;
import com.increff.model.products.UploadResult;
import com.increff.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductDto dto;

    @ApiOperation(value = "Add a product")
    @PostMapping
    public ProductData add(@Valid @RequestBody ProductForm form) throws ApiException {
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
    public ProductData update(@PathVariable Long id, @Valid @RequestBody ProductUpdateForm form) throws ApiException {
        return dto.update(id, form);
    }

    @ApiOperation(value = "Upload products from TSV file")
    @PostMapping("/upload")
    public UploadResult<ProductData> upload(@RequestParam("file") MultipartFile file) throws ApiException {
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
    public void delete(@PathVariable Long id) throws ApiException {
        dto.delete(id);
    }
}
