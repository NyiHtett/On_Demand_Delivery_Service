package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

// Timestamps are epoch milliseconds so the browser can show them in its own time zone.
public record OrderResponse(
    Long orderId,
    Long customerId,
    Long cartId,
    String orderStatus,
    String deliveryAddress,
    BigDecimal subtotal,
    BigDecimal deliveryFee,
    BigDecimal totalWeight,
    List<OrderItemResponse> items,
    Long createdAt,
    // When the robot carrying this order leaves (or left) the store; null until it is queued.
    Long estimatedDispatch,
    // When the robot reaches this order's address; null until the robot is dispatched.
    Long estimatedArrival
) {}
