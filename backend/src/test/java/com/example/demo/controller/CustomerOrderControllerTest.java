package com.example.demo.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerOrderController.class)
@Import(SecurityConfig.class)
public class CustomerOrderControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    OrderService orderService;
    @MockitoBean
    UserDao userDao;

    private void loginAs(String token, String userType) {
        when(userDao.getUserBySessionId(token))
            .thenReturn(Optional.of(new UserResponse(1L, "test-name", "test@email.com", userType)));
    }

    private OrderResponse order(String status) {
        return new OrderResponse(5L, 1L, 3L, status, "777 The Alameda", new BigDecimal("2.00"),
            BigDecimal.ZERO, new BigDecimal("0.800"), List.of(), null, null, null);
    }

    @Test
    public void getOwnOrder() throws Exception {
        loginAs("customer-token", "CUSTOMER");
        when(orderService.getOrderForCustomer(1L, 5L)).thenReturn(order("Ordered"));

        mockMvc.perform(get("/api/customer/orders/5").header("Authorization", "Bearer customer-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(5));
    }

    @Test
    public void someoneElsesOrderNotFound() throws Exception {
        loginAs("customer-token", "CUSTOMER");
        when(orderService.getOrderForCustomer(1L, 5L)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Order not found."));

        mockMvc.perform(get("/api/customer/orders/5").header("Authorization", "Bearer customer-token"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void cancelReturnsCancelledOrder() throws Exception {
        loginAs("customer-token", "CUSTOMER");
        when(orderService.cancelForCustomer(1L, 5L)).thenReturn(order("Cancelled"));

        mockMvc.perform(post("/api/customer/orders/5/cancel").header("Authorization", "Bearer customer-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderStatus").value("Cancelled"));
    }

    @Test
    public void employeeForbidden() throws Exception {
        loginAs("employee-token", "EMPLOYEE");

        mockMvc.perform(get("/api/customer/orders/5").header("Authorization", "Bearer employee-token"))
            .andExpect(status().isForbidden());
    }
}
