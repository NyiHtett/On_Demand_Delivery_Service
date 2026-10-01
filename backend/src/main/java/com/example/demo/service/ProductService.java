package com.example.demo.service;

import com.example.demo.dao.ProductDao;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.example.demo.dto.UpdateQuantityRequest;
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

    public ProductResponse updateQuantity(long productId, UpdateQuantityRequest req) {
        if (req == null || req.quantity() == null || req.quantity() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity cannot be less than 0, cannot be null");
        }
        int numUpdate = productDao.updateQuantity(productId, req.quantity());
        if (numUpdate == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
        }

        return getProduct(productId);
    }
}
