package com.example.demo.dto;

public record AddressValidationResponse (
    boolean valid,
    String message,
    String street,
    String city,
    String state,
    String zip
) {
    public String toString() {return (street + ", " + city + ", " + state + ", " + zip);}
}
