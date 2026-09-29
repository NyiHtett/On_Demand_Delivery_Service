package com.example.demo.model;

import java.time.LocalDateTime;

// record class for User entity
public record User(
        Long customerId, //INT in mySQL
        String name,
        String email,
        String phone, 
        String address,
        String passwordHash,
        String userType,
        LocalDateTime createdAt, // LocalDateTime in Java, Timestamp in MySQL
        LocalDateTime updatedAt
) {
}
