package com.example.demo.controller;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.demo.config.SecurityConfig;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.CreateProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.UpdateProductRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.service.ProductService;
import java.math.BigDecimal;

@WebMvcTest(ProductController.class) 
@Import(SecurityConfig.class)
public class ProductControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProductService productService;
    @MockitoBean
    UserDao userDao;

    ProductResponse apple = new ProductResponse(
        1L, "Apple", "fresh apples", new BigDecimal(".400"), new BigDecimal("1.00"), null, 100
    );

    private void loginAs(String token, String userType) {
        when(userDao.getUserBySessionId(token))
        .thenReturn(Optional.of(new UserResponse(1L, "test-name", "testemail@email.com", userType)));
    }

    @Test
    public void getProductWithoutLoginOk() throws Exception {
        when(productService.getInventory()).thenReturn(List.of(apple));

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Apple"));
    }

    @Test
    public void updateProductCustomerForbidden() throws Exception {
        loginAs("customer-token", "CUSTOMER");

        mockMvc.perform(put("/api/products/1")
            .header("Authorization", "Bearer customer-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"quantity\":50,\"unitPrice\":1.00,\"unitWeight\":0.400,\"description\":\"fresh apples\",\"imageUrl:\":null}"))
            .andExpect(status().isForbidden());

        verify(productService, never()).updateProduct(anyLong(), any());
    }

    @Test
    public void updateProductEmployeeOk() throws Exception {
        loginAs("employee-token", "EMPLOYEE");

        ProductResponse appleUpdated = new ProductResponse(
            1L, "Apple", "fresh apples", new BigDecimal(".400"), new BigDecimal("1.00"), null, 50
        );
        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(appleUpdated);

        mockMvc.perform(put("/api/products/1")
                        .header("Authorization", "Bearer employee-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":50,\"unitPrice\":1.00,\"unitWeight\":0.400,\"description\":\"fresh apples\",\"imageUrl:\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(50));

        verify(productService).updateProduct(eq(1L), any(UpdateProductRequest.class));
    }

    @Test
    public void createProductWithoutLoginUnauthorized() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Banana\",\"descrioption\":\"banana testing testing\",\"unitWeight\":3.512,\"unitPrice\":5.12,\"imageUrl\":null,\"quantity\":100}"))
                        .andExpect(status().isUnauthorized());

        verify(productService, never()).createProduct(any());
    }

    @Test
    public void deleteProductEmployeeNoContent() throws Exception {
        loginAs("employee-token", "EMPLOYEE");

        mockMvc.perform(delete("/api/products/1")
                        .header("Authorization", "Bearer employee-token"))
                        .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    public void getProductMissingNotFound() throws Exception {
        when(productService.getProduct(100L)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Product not found."));

        mockMvc.perform(get("/api/products/100"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Product not found."));
    }
}