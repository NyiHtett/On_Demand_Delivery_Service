package com.example.demo.controller;

import com.example.demo.dto.ProductResponse;
import com.example.demo.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.UpdateProductRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.demo.dto.CreateProductRequest;
import org.springframework.web.bind.annotation.PostMapping;


import java.util.List;

@RestController
@RequestMapping("api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getInventory() {
        return productService.getInventory();
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable long productId) {
        return productService.getProduct(productId);
    }

    @PutMapping("/{productId}")
    public ProductResponse updateProduct(@PathVariable long productId, @RequestBody UpdateProductRequest req) {
        return productService.updateProduct(productId, req);
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody CreateProductRequest req) {
        return productService.createProduct(req);
    }
}
