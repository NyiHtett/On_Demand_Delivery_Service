package com.example.demo.dto;

public record AccountRequest(
    String name,
    String email,
    String address,
    String phoneNumber
) {
}