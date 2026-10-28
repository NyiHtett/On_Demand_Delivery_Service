package com.example.demo.dao;

import com.example.demo.dto.TrackingOrderResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TrackingDao {
    private static final String DEFAULT_PICKUP_ADDRESS = "OFS Market, San Jose, CA";
    private final JdbcTemplate jdbcTemplate;

    public TrackingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TrackingOrderResponse> getActiveOrders() {
        return jdbcTemplate.query("""
            SELECT o.order_id,
                   COALESCE(u.user_name, 'Guest customer') AS customer_name,
                   o.delivery_address,
                   o.order_status,
                   COALESCE(dt.delivery_task_status, 'Not Started') AS delivery_task_status,
                   o.created_at,
                   dto.estimated_arrival
            FROM orders o
            LEFT JOIN users u ON u.user_id = o.customer_id
            LEFT JOIN delivery_task_orders dto ON dto.order_id = o.order_id
            LEFT JOIN delivery_tasks dt ON dt.task_id = dto.task_id
            WHERE o.order_status IN ('Active', 'Ordered', 'Shipping')
            ORDER BY o.created_at ASC, o.order_id ASC
            """, (rs, rowNum) -> new TrackingOrderResponse(
                rs.getLong("order_id"),
                rs.getString("customer_name"),
                rs.getString("delivery_address"),
                rs.getString("order_status"),
                rs.getString("delivery_task_status"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("estimated_arrival") == null
                    ? null
                    : rs.getTimestamp("estimated_arrival").toLocalDateTime()
            ));
    }

    public String getPickupAddress() {
        List<String> addresses = jdbcTemplate.query(
            "SELECT pickup_address FROM delivery_tasks WHERE delivery_task_status IN ('Not Started', 'En Route') ORDER BY assigned_at DESC LIMIT 1",
            (rs, rowNum) -> rs.getString("pickup_address")
        );
        // Active orders can exist before a delivery task is assigned. Start those
        // routes from the store so they can still be tracked in the manager view.
        return addresses.isEmpty() ? DEFAULT_PICKUP_ADDRESS : addresses.get(0);
    }

    public String getRouteStatus() {
        List<String> statuses = jdbcTemplate.query(
            "SELECT delivery_task_status FROM delivery_tasks WHERE delivery_task_status IN ('Not Started', 'En Route') ORDER BY assigned_at DESC LIMIT 1",
            (rs, rowNum) -> rs.getString("delivery_task_status")
        );
        return statuses.isEmpty() ? "Not Started" : statuses.get(0);
    }
}
