package com.example.demo.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long orderItemId,
    Long productId,
    String productName,
    BigDecimal unitWeight,
    BigDecimal unitPrice,
    Integer quantity
) {}