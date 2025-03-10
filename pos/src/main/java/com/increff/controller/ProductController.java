package com.increff.controller;

import com.increff.model.ProductData;
import com.increff.model.ProductForm;
import com.increff.model.ProductUploadForm;
import com.increff.dto.ProductDto;
import com.increff.service.ApiException;
import com.increff.util.TsvUtil;
import com.increff.model.UploadResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Api
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductDto dto;

    @ApiOperation(value = "Add a product")
    @PostMapping
    public ProductData add(@Valid @RequestBody ProductForm form) {
        return dto.add(form);
    }

    @ApiOperation(value = "Get a product by ID")
    @GetMapping("/{id}")
    public ProductData get(@PathVariable Long id) {
        return dto.get(id);
    }

    @ApiOperation(value = "Get list of all products")
    @GetMapping
    public List<ProductData> getAll() {
        return dto.getAll();
    }

    @ApiOperation(value = "Update a product")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody ProductForm form) throws ApiException {
        dto.update(id, form);
    }

    @ApiOperation(value = "Delete a product")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        dto.delete(id);
    }

    
    @ApiOperation(value = "Upload Products through TSV")
    @PostMapping("/upload")
    public UploadResult<ProductData> upload(@RequestParam("file") MultipartFile file) throws ApiException {
        try {
            List<ProductForm> forms = TsvUtil.readProductsFromTsv(file);
            return dto.uploadProducts(forms);
        } catch (IOException e) {
            throw new ApiException("Error reading file: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Search products")
    @PostMapping("/search")
    public List<ProductData> search(@RequestBody ProductForm form) {
        return dto.search(form);
    }
}
