package com.example.demo.service;

import com.example.demo.dao.ProductDao;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<ProductResponse> getInventory() {
        return productDao.getAllProducts();
    }

    public ProductResponse getProduct(long productId) {
        return productDao.getProduct(productId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found."));
    }
}
