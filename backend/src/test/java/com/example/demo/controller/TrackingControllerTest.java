package com.example.demo.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.QueueBatchResponse;
import com.example.demo.dto.RobotRunResponse;
import com.example.demo.dto.TrackingResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.TrackingService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * No authentication -> 401 
 * Customer Access.  -> 403
 * Employee Access   -> 200
 * Tracking response fields
 * TrackingControllerTest
 */
@WebMvcTest(TrackingController.class)
@Import(SecurityConfig.class)
class TrackingControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TrackingService trackingService;

    @MockitoBean
    UserDao userDao;

    private void loginAs(String token, String userType) {
        when(userDao.getUserBySessionId(token))
            .thenReturn(Optional.of(new UserResponse(
                1L,
                "test-name",
                "test@example.com",
                userType
            )));
    }

    private TrackingResponse trackingResponse() {
        QueueBatchResponse queue = new QueueBatchResponse(
            7L,
            1,
            new BigDecimal("25.0"),
            10,
            new BigDecimal("200.0"),
            1000L,
            1300L,
            List.of()
        );

        RobotRunResponse robot = new RobotRunResponse(
            6L,
            "Shipping",
            900L,
            600,
            12000,
            "encoded-polyline",
            List.of()
        );

        return new TrackingResponse(
            1500L,
            "150 E San Fernando St, San Jose, CA 95112",
            queue,
            List.of(robot),
            null,
            true
        );
    }

    //No authention -> 401
    @Test
    void trackingWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/manager/tracking"))
            .andExpect(status().isUnauthorized());
    }

    //Customer      -> 402
    @Test
    void customerCannotAccessTracking() throws Exception {
        loginAs("customer-token", "CUSTOMER");

        mockMvc.perform(get("/api/manager/tracking")
                .header("Authorization", "Bearer customer-token"))
            .andExpect(status().isForbidden());
    }

    //Employee authorized    -> 200
    @Test
    void employeeCanAccessTracking() throws Exception {
        loginAs("employee-token", "EMPLOYEE");
        when(trackingService.getTracking(null)).thenReturn(trackingResponse());

        mockMvc.perform(get("/api/manager/tracking")
                .header("Authorization", "Bearer employee-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pickupAddress")
                .value("150 E San Fernando St, San Jose, CA 95112"))
            .andExpect(jsonPath("$.queue.taskId").value(7))
            .andExpect(jsonPath("$.robots[0].encodedPolyline")
                .value("encoded-polyline"))
            .andExpect(jsonPath("$.serverHasMapsKey").value(true));

        verify(trackingService).getTracking(null);
    }

    //Forwarding the API key and testing the status
    @Test
    void trackingForwardsGoogleMapsApiKey() throws Exception {
        loginAs("employee-token", "EMPLOYEE");
        when(trackingService.getTracking("test-map-key"))
            .thenReturn(trackingResponse());

        mockMvc.perform(get("/api/manager/tracking")
                .header("Authorization", "Bearer employee-token")
                .header("X-Google-Maps-Api-Key", "test-map-key"))
            .andExpect(status().isOk());

        verify(trackingService).getTracking("test-map-key");
    }
}
