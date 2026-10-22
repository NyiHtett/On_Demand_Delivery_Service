package com.example.demo.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemID,
        Long productID,
        String productName,
        BigDecimal unitPrice,
        BigDecimal unitWeight,
        int quantity,
        String imageUrl
) {}
