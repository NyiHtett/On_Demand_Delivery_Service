package com.example.demo.dao;

import com.example.demo.dto.CartItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CartDao {
    private final JdbcTemplate jdbcTemplate;

    public CartDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // userID -> CartID
    public boolean containsActiveCart(long customerID) {
        return false; // Simply see if it does exist
    }

    public Long getUserCartID(long customerID) {
        return null;
    }

    // CartID -> List<CartItemResponse>
    public List<CartItemResponse> getCartItems(long cartID) {
        return null;
    }
}
