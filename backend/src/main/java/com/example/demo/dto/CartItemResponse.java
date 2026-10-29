package com.example.demo.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemID,
        Long productID,
        String productName,
        BigDecimal unitPrice,
        BigDecimal unitWeight,
        int quantity,
        String imageUrl,
        // Current stock, so the cart page can cap the + button (null if the product was deleted).
        Integer stock
) {}
