package com.example.demo.service;

import com.example.demo.dao.ProductDao;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.example.demo.dto.UpdateProductRequest;
import com.example.demo.dto.CreateProductRequest;
import java.util.List;
import java.math.BigDecimal;

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

    public ProductResponse updateProduct(long productId, UpdateProductRequest req) {
        if (req == null || req.quantity() == null || req.unitPrice() == null || req.unitWeight() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "quantity, price, and weight can't be null");
        }

        if (req.quantity() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "stock has to be positive");
        }

        if (req.unitPrice().compareTo(BigDecimal.ZERO) <= 0 || req.unitWeight().compareTo(BigDecimal.ZERO) <= 0) { //using big decimal for precision, since floating point gives a lot of errors
            throw new ApiException(HttpStatus.BAD_REQUEST, "price and weight have to be positive numbers");
        }

        int numUpdate = productDao.updateProduct(productId, req.quantity(), req.unitPrice(), req.unitWeight());
        if (numUpdate == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
        }

        return getProduct(productId);
    }

    public ProductResponse createProduct(CreateProductRequest req) {
        if (req == null || req.name() == null || req.name().isBlank() || req.unitPrice() == null || req.unitWeight() == null || req.quantity() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "you need to enter these following fields at least: name, price, weight, and quantity");
        }

        if (req.quantity() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "stock has to be positive");
        }

        if (req.unitPrice().compareTo(BigDecimal.ZERO) <= 0 || req.unitWeight().compareTo(BigDecimal.ZERO) <= 0) { //using big decimal for precision, since floating point gives a lot of errors
            throw new ApiException(HttpStatus.BAD_REQUEST, "price and weight have to be positive numbers");
        }

        long newId = productDao.createProduct(
            req.name().trim(),
            req.description(),
            req.unitWeight(),
            req.unitPrice(),
            req.imageURL(),
            req.quantity()
        );

        return getProduct(newId);
    }
}
