package com.example.demo.dto;

// deliveryAddress is optional; the account address is used when it is blank.
public record CheckoutRequest(AddressRequest deliveryAddress) {}
