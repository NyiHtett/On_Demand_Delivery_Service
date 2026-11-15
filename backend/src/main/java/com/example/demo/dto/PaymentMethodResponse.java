package com.example.demo.dto;

public record PaymentMethodResponse (
    Long id,
    String cardType, 
    String lastFour, 
    String billingName, 
    String expirationDate, 
    boolean defaultMethod
) {}



