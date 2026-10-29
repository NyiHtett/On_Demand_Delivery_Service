package com.example.demo.dto;

// sessionId comes back from Stripe; orderId is used when checkout ran without Stripe.
public record ConfirmCheckoutRequest(String sessionId, Long orderId) {}
