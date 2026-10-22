package com.example.demo.dao;

import com.example.demo.dto.CartItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CartDao {
    private final JdbcTemplate jdbcTemplate;

    public CartDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // userID -> CartID
    public boolean containsActiveCart(long customerID) {
        String sql = "SELECT count(*) FROM shopping_carts WHERE customer_id = ? AND table_status = 'Active'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, customerID);
        return count != null && count > 0;
    }

    public Long getUserCartID(long customerID) {
        String sql = "SELECT cart_id FROM shopping_carts WHERE customer_id = ? AND table_status = 'Active' ORDER BY cart_id DESC LIMIT 1";
        return jdbcTemplate.queryForObject(sql, Long.class, customerID);
    }

    // CartID -> List<CartItemResponse>
    public List<CartItemResponse> getCartItems(long cartID) {
        String sql = "SELECT ci.cart_item_id, ci.product_id, ci.product_name, ci.unit_price, ci.unit_weight, ci.quantity, p.image_url " +
                "FROM cart_items ci " +
                "LEFT JOIN products p ON p.product_id = ci.product_id " +
                "WHERE ci.cart_id = ? " +
                "ORDER BY ci.cart_item_id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CartItemResponse(
                rs.getLong("cart_item_id"),
                rs.getObject("product_id", Long.class),
                rs.getString("product_name"),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("unit_weight"),
                rs.getInt("quantity"),
                rs.getString("image_url")
                ), cartID);
    }
}
