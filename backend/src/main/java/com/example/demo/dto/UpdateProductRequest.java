package com.example.demo.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(Integer quantityChange, BigDecimal unitPrice, BigDecimal unitWeight, String description, String imageUrl) {}