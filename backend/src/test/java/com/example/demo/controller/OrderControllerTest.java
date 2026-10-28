package com.example.demo.controller;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.UpdateOrderStatusRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.service.OrderService;
import java.math.BigDecimal;


@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
public class OrderControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    OrderService orderService;
    @MockitoBean
    UserDao userDao;

    private void loginAs(String token, String userType) {
        when(userDao.getUserBySessionId(token))
        .thenReturn(Optional.of(new UserResponse(1L, "test-name", "testemail@email.com", userType)));
    }

    @Test
    public void getOrdersNoTokensUnauthorized() throws Exception {
        mockMvc.perform(get("/api/orders"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    public void getOrdersCustomerForbidden() throws Exception {
        loginAs("customer-token", "CUSTOMER");

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer customer-token")).andExpect(status().isForbidden());
    }

    @Test
    public void getOrdersEmployeeGood() throws Exception {
        loginAs("employee-token", "EMPLOYEE");

        OrderResponse order = new OrderResponse(
            1L,
            1L,
            1L,
            "1234567891011121",
            "Active",
            "One Washington Square",
            new BigDecimal("22.03"),
            new BigDecimal("0.00"),
            new BigDecimal("13.601"),
            List.of() //empty list for the order items, just for the test
        );
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer employee-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderId").value(1))
            .andExpect(jsonPath("$[0].orderStatus").value("Active"));
    }

    @Test
    public void updateStatusEmployeeNoContent() throws Exception {
        loginAs("employee-token", "EMPLOYEE");

        mockMvc.perform(put("/api/orders/1/status").header("Authorization", "Bearer employee-token").contentType(MediaType.APPLICATION_JSON).content("{\"orderStatus\":\"Shipping\"}")).andExpect(status().isNoContent());

        verify(orderService).updateOrderStatus(1L, new UpdateOrderStatusRequest("Shipping"));
    }

    @Test
    public void updateStatusLockedBadRequest() throws Exception {
        loginAs("employee-token", "EMPLOYEE");

        doThrow(new ApiException(HttpStatus.BAD_REQUEST, "cannot change from shipping/completed")).when(orderService).updateOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class));

        mockMvc.perform(put("/api/orders/1/status").header("Authorization", "Bearer employee-token").contentType(MediaType.APPLICATION_JSON).content("{\"orderStatus\":\"Cancelled\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("cannot change from shipping/completed"));
    }
}