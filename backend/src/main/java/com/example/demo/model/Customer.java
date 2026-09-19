package com.example.demo.model;

import java.time.LocalDateTime;

// record class for Customer entity
public record Customer(
        Long customerId,
        String name,
        String email,
        String address,
        String phone, 
        LocalDateTime createdAt // LocalDateTime in Java, Timestamp in MySQL
) {
}