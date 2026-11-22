package com.example.demo.dto;

public record AddressRequest (
    String street,
    String city,
    String state,
    String zip
) {}