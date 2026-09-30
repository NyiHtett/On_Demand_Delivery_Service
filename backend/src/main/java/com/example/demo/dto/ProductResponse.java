package com.example.demo.dto;

import java.math.BigDecimal;

public record ProductResponse(
    Long productId,
    String name,
    String description,
    BigDecimal unitWeight,
    BigDecimal unitPrice,
    String imageUrl,
    Integer quantity
) {}
