package com.example.demo.dto;

import java.math.BigDecimal;

public record TrackingOrderResponse(
    long orderId,
    String customerName,
    String deliveryAddress,
    String orderStatus,
    BigDecimal totalWeight,
    Integer stopNumber,
    Double latitude,
    Double longitude,
    // Epoch milliseconds; null until the robot is dispatched.
    Long estimatedArrival
) {}
