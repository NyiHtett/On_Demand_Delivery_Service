package com.example.demo.dto;

// url is the Stripe Checkout page, or the confirmation page when Stripe is not configured.
public record CheckoutSessionResponse(Long orderId, String url) {}
