package com.example.demo.dto;
import java.math.BigDecimal;
import java.util.List;

// Entire cart line
// List of all items in an object + applied business logic
public record CartResponse (
        Long CartId,
        List<CartItemResponse> items,
        BigDecimal total,
        BigDecimal subtotal,
        BigDecimal DeliveryFee,
        BigDecimal totalWeight
) {}

