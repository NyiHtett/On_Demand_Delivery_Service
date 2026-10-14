package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
    Long orderId,
    Long customerId,
    Long cartId,
    String cardNumber,
    String orderStatus,
    String deliveryAddress,
    BigDecimal subtotal,
    BigDecimal deliveryFee,
    BigDecimal totalWeight,
    List<OrderItemResponse> items
) {}