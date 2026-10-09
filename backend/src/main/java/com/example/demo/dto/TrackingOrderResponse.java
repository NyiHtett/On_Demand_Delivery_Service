package com.example.demo.dto;

import java.time.LocalDateTime;

public record TrackingOrderResponse(
    long orderId,
    String customerName,
    String deliveryAddress,
    String orderStatus,
    String deliveryTaskStatus,
    LocalDateTime createdAt,
    LocalDateTime estimatedArrival
) {}
