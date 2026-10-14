package com.example.demo.dao;

import com.example.demo.dto.OrderItemResponse;
import com.example.demo.dto.OrderResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderDao {
    private final JdbcTemplate jdbcTemplate;

    public OrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrderItemResponse> getOrderItems (long orderId) {
        return jdbcTemplate.query(
            "SELECT order_item_id, product_id, product_name, unit_weight, unit_price, quantity FROM order_items WHERE order_id = ?",
            (resultSet, rowNumber) -> mapOrderItem(resultSet),
            orderId
        );
    }

    private OrderItemResponse mapOrderItem(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new OrderItemResponse(
            resultSet.getLong("order_item_id"),
            resultSet.getObject("product_id", Long.class), //i do getObject here since the product id might be null and crash getlong
            resultSet.getString("product_name"),
            resultSet.getBigDecimal("unit_weight"),
            resultSet.getBigDecimal("unit_price"),
            resultSet.getInt("quantity")
        );
    }

    public List<OrderResponse>getAllOrders() {
        return jdbcTemplate.query(
            "SELECT order_id, customer_id, cart_id, card_number, order_status, delivery_address, subtotal, delivery_fee, total_weight FROM orders ORDER BY created_at DESC",
            (resultSet, rowNumber) -> mapOrder(resultSet)   
        );
    }

    private OrderResponse mapOrder(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        long orderId = resultSet.getLong("order_id");

        return new OrderResponse(
            orderId,
            resultSet.getObject("customer_id", Long.class),
            resultSet.getLong("cart_id"),
            resultSet.getString("card_number"),
            resultSet.getString("order_status"),
            resultSet.getString("delivery_address"),
            resultSet.getBigDecimal("subtotal"),
            resultSet.getBigDecimal("delivery_fee"),
            resultSet.getBigDecimal("total_weight"),
            getOrderItems(orderId)
        );
    }

    public String getOrderStatus(long orderId) {
        //making this a list to prevent crashes just in case there is a wrong orderId
        List<String> statuses = jdbcTemplate.queryForList(
            "SELECT order_status FROM orders WHERE order_id = ?",
            String.class,
            orderId
        );

        if (statuses.isEmpty()) {
            return null;
        }
        return statuses.get(0);
    }

    public int updateOrderStatus(long orderId, String orderStatus) {
        return jdbcTemplate.update(
            "UPDATE orders SET order_status = ? WHERE order_id = ?",
            orderStatus,
            orderId
        );
    }
}