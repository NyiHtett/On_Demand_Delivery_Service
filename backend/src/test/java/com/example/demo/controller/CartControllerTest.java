package com.example.demo.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.CartResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.service.CartService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
public class CartControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CartService cartService;
    @MockitoBean
    UserDao userDao;

    CartResponse emptyCart = new CartResponse(1L, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    // the logged in user is always user 1
    private void loginAs(String token, String userType) {
        when(userDao.getUserBySessionId(token))
            .thenReturn(Optional.of(new UserResponse(1L, "test-name", "test@email.com", userType)));
    }

    @Test
    public void noTokenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    public void employeeForbidden() throws Exception {
        loginAs("employee-token", "EMPLOYEE");

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer employee-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    public void addItemUsesLoggedInUser() throws Exception {
        loginAs("customer-token", "CUSTOMER");
        when(cartService.addItem(1L, 7L, 2)).thenReturn(emptyCart);

        mockMvc.perform(post("/api/cart/items").header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"productId\":7,\"quantity\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService).addItem(1L, 7L, 2);
    }

    @Test
    public void updateItemSetsQuantity() throws Exception {
        loginAs("customer-token", "CUSTOMER");
        when(cartService.updateItem(1L, 7L, 3)).thenReturn(emptyCart);

        mockMvc.perform(put("/api/cart/items/7").header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
            .andExpect(status().isOk());

        verify(cartService).updateItem(1L, 7L, 3);
    }

    @Test
    public void removeItemDeletes() throws Exception {
        loginAs("customer-token", "CUSTOMER");
        when(cartService.removeItem(1L, 7L)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/cart/items/7").header("Authorization", "Bearer customer-token"))
            .andExpect(status().isOk());

        verify(cartService).removeItem(1L, 7L);
    }

    @Test
    public void outOfStockErrorReachesClient() throws Exception {
        loginAs("customer-token", "CUSTOMER");
        when(cartService.addItem(1L, 7L, 99)).thenThrow(new ApiException(HttpStatus.CONFLICT, "Only 40 Milk in stock."));

        mockMvc.perform(post("/api/cart/items").header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"productId\":7,\"quantity\":99}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Only 40 Milk in stock."));
    }
}
