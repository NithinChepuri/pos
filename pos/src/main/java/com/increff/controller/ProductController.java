package com.increff.controller;

import com.increff.model.ProductData;
import com.increff.model.ProductForm;
import com.increff.model.ProductSearchForm;
import com.increff.model.ProductUploadForm;
import com.increff.dto.ProductDto;
import com.increff.util.TsvUtil;
import com.increff.model.UploadResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ProductData add(@RequestBody ProductForm form) {
        return dto.add(form);
    }

    @ApiOperation(value = "Get a product by ID")
    @GetMapping("/{id}")
    public ProductData get(@PathVariable Long id) {
        return dto.get(id);
    }

    @ApiOperation(value = "Get list of all products with pagination")
    @GetMapping
    public List<ProductData> getAll(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
        return dto.getAll(page, size);
    }

    @ApiOperation(value = "Update a product")
    @PutMapping("/{id}")
    public ResponseEntity<ProductData> update(@PathVariable Long id, @RequestBody ProductForm form) {
        return dto.updateProduct(id, form);
    }

    @ApiOperation(value = "Delete a product")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        dto.delete(id);
    }

    @ApiOperation(value = "Upload Products through TSV")
    @PostMapping("/upload")
    public ResponseEntity<UploadResult<ProductData>> upload(@RequestParam("file") MultipartFile file) {
        return dto.processUpload(file);
    }

    @ApiOperation(value = "Search products with pagination")
    @PostMapping("/search")
    public List<ProductData> search(
        @RequestBody ProductSearchForm form,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "1") int size) {
        return dto.search(form, page, size);
    }
}
