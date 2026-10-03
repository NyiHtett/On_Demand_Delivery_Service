package com.example.demo.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(Integer quantity, BigDecimal unitPrice, BigDecimal unitWeight) {}