package com.example.demo.dto;

public record SignUpRequest(
  String name, 
  String email, 
  String password
) {}

