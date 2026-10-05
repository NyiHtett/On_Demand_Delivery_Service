package com.example.demo.dto;

import java.math.BigDecimal;

public record CreateProductRequest (
    String name,
    String description,
    BigDecimal unitWeight,
    BigDecimal unitPrice,
    String imageURL,
    Integer quantity
) {}