package com.example.demo.dao;

import com.example.demo.dto.CartItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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

    // Carts are created lazily the first time a customer adds an item.
    public long createCart(long customerID) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO shopping_carts (customer_id, table_status) VALUES (?, 'Active')",
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, customerID);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    // CartID -> List<CartItemResponse>
    // Price and weight come from the product when it still exists, so the cart always shows current prices.
    public List<CartItemResponse> getCartItems(long cartID) {
        String sql = "SELECT ci.cart_item_id, ci.product_id, ci.product_name, " +
                "COALESCE(p.unit_price, ci.unit_price) AS unit_price, " +
                "COALESCE(p.unit_weight, ci.unit_weight) AS unit_weight, " +
                "ci.quantity, p.image_url, p.quantity AS stock " +
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
                rs.getString("image_url"),
                rs.getObject("stock", Integer.class)
                ), cartID);
    }

    public int getItemQuantity(long cartID, long productID) {
        List<Integer> quantities = jdbcTemplate.queryForList(
            "SELECT quantity FROM cart_items WHERE cart_id = ? AND product_id = ?",
            Integer.class, cartID, productID
        );
        return quantities.isEmpty() ? 0 : quantities.get(0);
    }

    // Inserts the product into the cart, or overwrites its quantity if it is already there.
    public void upsertItem(long cartID, long productID, int quantity) {
        jdbcTemplate.update(
            "INSERT INTO cart_items (cart_id, product_id, product_name, unit_weight, unit_price, quantity) " +
            "SELECT ?, product_id, product_name, unit_weight, unit_price, ? FROM products WHERE product_id = ? " +
            "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), unit_weight = VALUES(unit_weight)",
            cartID, quantity, productID
        );
    }

    public int deleteItem(long cartID, long productID) {
        return jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?", cartID, productID);
    }

    public int setCartStatus(long cartID, String status) {
        return jdbcTemplate.update("UPDATE shopping_carts SET table_status = ? WHERE cart_id = ?", status, cartID);
    }
}
