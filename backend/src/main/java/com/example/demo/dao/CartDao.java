package com.example.demo.dao;

import com.example.demo.dto.CartItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

public class CartDao {
    private final JdbcTemplate jdbcTemplate;

    public CartDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // userID -> CartID
    public Optional<Long> getUserCart(long customerID) {
        return null;
    }

    // CartID -> List<CartItemResponse>
    public List<CartItemResponse> findItems(long cartID) {
        return null;
    }
}
