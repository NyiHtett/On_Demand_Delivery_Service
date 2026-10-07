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

import com.example.demo.dao.InventoryUpdateDao;
import com.example.demo.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class ProductService {
    private final ProductDao productDao;
    private final InventoryUpdateDao inventoryUpdateDao;

    public ProductService(ProductDao productDao, InventoryUpdateDao inventoryUpdateDao) {
        this.productDao = productDao;
        this.inventoryUpdateDao = inventoryUpdateDao;
    }

    public List<ProductResponse> getInventory() {
        return productDao.getAllProducts();
    }

    public ProductResponse getProduct(long productId) {
        return productDao.getProduct(productId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found."));
    }

    @Transactional
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

        ProductResponse preupdate = getProduct(productId);

        int numUpdate = productDao.updateProduct(productId, req.quantity(), req.unitPrice(), req.unitWeight(), req.description(), req.imageUrl());
        if (numUpdate == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
        }

        //going to add a new update row in the database for every quantity that's been updated
        String name = preupdate.name();
        if (!Objects.equals(preupdate.quantity(), req.quantity())) {
            inventoryUpdate(productId, name, "Updated", "quantity", preupdate.quantity(), req.quantity(), null);
        }

        if (preupdate.unitPrice().compareTo(req.unitPrice()) != 0) {
            inventoryUpdate(productId, name, "Updated", "unit_price", preupdate.unitPrice(), req.unitPrice(), null);
        }

        if (preupdate.unitWeight().compareTo(req.unitWeight()) != 0) {
            inventoryUpdate(productId, name, "Updated", "unit_weight", preupdate.unitWeight(), req.unitWeight(), null);
        } 

        if (!Objects.equals(preupdate.description(), req.description())) {
            inventoryUpdate(productId, name, "Updated", "product_description", preupdate.description(), req.description(), null);
        }

        if (!Objects.equals(preupdate.imageUrl(), req.imageUrl())) {
            inventoryUpdate(productId, name, "Updated", "image_url", preupdate.imageUrl (), req.imageUrl(), null);
        }

        return getProduct(productId);
    }

    @Transactional //this part is to make sure that both DAO writes happen together (if one fails then none happen)
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
            req.imageUrl(),
            req.quantity()
        );

        ProductResponse newProduct = getProduct(newId);
        inventoryUpdate(newId, newProduct.name(), "Created", null, null, null, "created new product");

        return newProduct;
    }

    @Transactional
    public void deleteProduct(long productId) {
        ProductResponse product = getProduct(productId);
        inventoryUpdate(productId, product.name(), "Deleted", null, null, null, "deleted this product");
        int deleteTest = productDao.deleteProduct(productId);
        if (deleteTest == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "product can't be found");
        }
    }

    private UserResponse currUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserResponse user) { //want to make sure that it's a user response that is getting returned.
            return user;
        }
        return null;
    }

    private void inventoryUpdate(long productId, String productName, String action, String field, Object oldVal, Object newVal, String description) {
        UserResponse user = currUser();
        inventoryUpdateDao.inventoryUpdate(productId, productName, user.id(), user.name(), action, field, Objects.toString(oldVal, null), Objects.toString(newVal, null), description);
    }
}
