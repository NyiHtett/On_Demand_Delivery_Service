package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.Optional;
import com.example.demo.dao.InventoryUpdateDao;
import com.example.demo.dao.ProductDao;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.UpdateProductRequest;
import com.example.demo.exception.ApiException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.CreateProductRequest;
import org.springframework.http.HttpStatus;

public class ProductServiceTest {
    ProductDao productDao = mock(ProductDao.class);
    InventoryUpdateDao inventoryUpdateDao = mock(InventoryUpdateDao.class);
    ProductService productService = new ProductService(productDao, inventoryUpdateDao);

    //just going to create a fake item the mock dao can work with, same values as seed
    ProductResponse apple = new ProductResponse(
        1L,
        "Apple",
        "fresh apples",
        new BigDecimal("0.400"),
        new BigDecimal("1.00"),
        null,   //the image urls were implemented when the employee puts the links in
        100
    );

    UserResponse employee = new UserResponse(2L, "Test Employee", "employee@test.com", "Employee");

    @BeforeEach
    void loginEmployee() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(employee, null, List.of())
        );
    }

    @AfterEach
    void logout() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getProductReturnsProduct(){
        when(productDao.getProduct(1L)).thenReturn(Optional.of(apple));

        ProductResponse result = productService.getProduct(1L);

        assertEquals("Apple", result.name());
    }

    @Test
    public void getProductMissingThrowsNotFound() {
        when(productDao.getProduct(100L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> productService.getProduct(100L));
    }

    @Test
    public void updateProductRejectsMissingFields() {
        UpdateProductRequest req = new UpdateProductRequest(
            null,
            new BigDecimal("1.00"),
            new BigDecimal("0.400"),
            "fresh apples",
            null
        );

        assertThrows(ApiException.class, () -> productService.updateProduct(1L, req));

        verify(productDao, never()).updateProduct(anyLong(), anyInt(), any(), any(), any(), any());
    }

    @Test
    public void updateProductRejectsRemovingMoreThanStock() {
        UpdateProductRequest req = new UpdateProductRequest(
            -1000,
            new BigDecimal("1.00"),
            new BigDecimal("0.400"),
            "fresh apples",
            null
        );

        when(productDao.getProduct(1L)).thenReturn(Optional.of(apple));
        when(productDao.updateProduct(eq(1L), eq(-1000), any(), any(), any(), any())).thenReturn(0);

        ApiException e = assertThrows(ApiException.class, () -> productService.updateProduct(1L, req));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());

        verify(inventoryUpdateDao, never()).inventoryUpdate(anyLong(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test 
    public void updateProductRejecttsZeroPrice() {
        UpdateProductRequest req = new UpdateProductRequest(
            1,
            new BigDecimal("0.00"),
            new BigDecimal("0.400"),
            "fresh apples",
            null
        ); 

        assertThrows(ApiException.class, () -> productService.updateProduct(1L, req));

        verify(productDao, never()).updateProduct(anyLong(), anyInt(), any(), any(), any(), any());
    }

    @Test
    public void updateProductRejectsZeroWeight() {
        UpdateProductRequest req = new UpdateProductRequest(
            1,
            new BigDecimal("1.00"),
            new BigDecimal("0.000"),
            "fresh apples",
            null
        ); 

        assertThrows(ApiException.class, () -> productService.updateProduct(1L, req));

        verify(productDao, never()).updateProduct(anyLong(), anyInt(), any(), any(), any(), any());
    }

    @Test
    public void updateProductSavesAndLogsChange() {
        UpdateProductRequest req = new UpdateProductRequest(
            -50,
            new BigDecimal("1.00"),
            new BigDecimal("0.400"),
            "fresh apples",
            null
        );

        ProductResponse appleAfter = new ProductResponse(
            1L, "Apple", "fresh apples", new BigDecimal("0.400"), new BigDecimal("1.00"), null, 50
        );

        when(productDao.getProduct(1L)).thenReturn(Optional.of(apple), Optional.of(appleAfter));
        when(productDao.updateProduct(eq(1L), eq(-50), any(), any(), any(), any())).thenReturn(1);

        ProductResponse result = productService.updateProduct(1L, req);

        assertEquals(50, result.quantity());

        verify(productDao).updateProduct(1L, -50, new BigDecimal("1.00"), new BigDecimal("0.400"), "fresh apples", null);

        verify(inventoryUpdateDao).inventoryUpdate(1L, "Apple", 2L, "Test Employee", "Updated", "quantity", "100", "50", null);
        verify(inventoryUpdateDao, never()).inventoryUpdate(anyLong(), any(), any(), any(), any(), eq("unit_price"), any(), any(), any());
    }

    @Test
    public void createProductRejectsBlankName() {
        CreateProductRequest req = new CreateProductRequest(
            " ",
            "banana testing testing",
            new BigDecimal("1.00"),
            new BigDecimal("1.00"),
            null,
            100
        );

        assertThrows(ApiException.class, () -> productService.createProduct(req));

        verify(productDao, never()).createProduct(any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    public void createProductSavesAndLogsCreation() {
        CreateProductRequest req = new CreateProductRequest(
            "Banana",
            "banana testing testing",
            new BigDecimal("1.00"),
            new BigDecimal("1.00"),
            null,
            100
        );

        ProductResponse banana = new ProductResponse(
            15L, "Banana", "banana testing testing", new BigDecimal("1.00"), new BigDecimal("1.00"), null, 100
        );

        when(productDao.createProduct(eq("Banana"), any(), any(), any(), any(), anyInt())).thenReturn(15L);
        when(productDao.getProduct(15L)).thenReturn(Optional.of(banana));

        ProductResponse result = productService.createProduct(req);

        assertEquals(15L, result.productId());
        assertEquals("Banana", result.name());

        verify(productDao).createProduct("Banana", "banana testing testing", new BigDecimal("1.00"), new BigDecimal("1.00"), null, 100);

        verify(inventoryUpdateDao).inventoryUpdate(15L, "Banana", 2L, "Test Employee", "Created", null, null, null, "created new product");
    }

    @Test
    public void deleteProductMissingThrowsNotFound() {
        when(productDao.getProduct(100L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> productService.deleteProduct(100L)); //this ApiException comes from the dao method

        verify(productDao, never()).deleteProduct(anyLong());
        verify(inventoryUpdateDao, never()).inventoryUpdate(anyLong(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void deleteProductDeletesAndLogs() {
        when(productDao.getProduct(1L)).thenReturn(Optional.of(apple));
        when(productDao.deleteProduct(1L)).thenReturn(1);

        productService.deleteProduct(1L);

        verify(productDao).deleteProduct(1L);

        verify(inventoryUpdateDao).inventoryUpdate(1L, "Apple", 2L, "Test Employee", "Deleted", null, null, null, "deleted this product");
    }
}