package com.example.demo.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.ConfirmCheckoutRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.CheckoutService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CheckoutController.class)
@Import(SecurityConfig.class)
public class CheckoutControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CheckoutService checkoutService;
    @MockitoBean
    UserDao userDao;

    private void loginAsCustomer() {
        when(userDao.getUserBySessionId("customer-token"))
            .thenReturn(Optional.of(new UserResponse(1L, "test-name", "test@email.com", "CUSTOMER")));
    }

    @Test
    public void confirmPassesSessionId() throws Exception {
        loginAsCustomer();
        OrderResponse order = new OrderResponse(5L, 1L, 3L, "Ordered", "777 The Alameda", new BigDecimal("2.00"),
            BigDecimal.ZERO, new BigDecimal("0.800"), List.of(), null, null, null);
        when(checkoutService.confirm(1L, new ConfirmCheckoutRequest("cs_test_1", null))).thenReturn(order);

        mockMvc.perform(post("/api/checkout/confirm").header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"sessionId\":\"cs_test_1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderStatus").value("Ordered"));
    }

    @Test
    public void cancelReturnsNoContent() throws Exception {
        loginAsCustomer();

        mockMvc.perform(post("/api/checkout/cancel").header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"sessionId\":\"cs_test_1\"}"))
            .andExpect(status().isNoContent());

        verify(checkoutService).cancel(1L, "cs_test_1");
    }

    @Test
    public void noTokenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/checkout/confirm")
                .contentType(MediaType.APPLICATION_JSON).content("{\"orderId\":5}"))
            .andExpect(status().isUnauthorized());
    }
}
