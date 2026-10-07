
package com.example.demo.dto;
public record AccountResponse(
    Long user_id,
    String user_name,
    String email,
    String address,
    String phone, 
    String user_type
) {
}