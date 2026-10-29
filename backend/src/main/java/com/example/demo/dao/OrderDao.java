package com.example.demo.dao;

import com.example.demo.dto.OrderItemResponse;
import com.example.demo.dto.OrderResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderDao {
    private final JdbcTemplate jdbcTemplate;

    // Times are returned as epoch milliseconds (UNIX_TIMESTAMP uses the DB's own time zone,
    // which is what NOW() wrote them in), so Java and the browser never have to guess a zone.
    private static final String ORDER_SELECT = """
        SELECT o.order_id, o.customer_id, o.cart_id, o.order_status, o.delivery_address,
               o.subtotal, o.delivery_fee, o.total_weight,
               CAST(UNIX_TIMESTAMP(o.created_at) * 1000 AS SIGNED) AS created_ms,
               CAST(UNIX_TIMESTAMP(COALESCE(dt.start_time, dt.dispatch_at)) * 1000 AS SIGNED) AS dispatch_ms,
               CAST(UNIX_TIMESTAMP(dto.estimated_arrival) * 1000 AS SIGNED) AS arrival_ms
        FROM orders o
        LEFT JOIN delivery_task_orders dto ON dto.order_id = o.order_id
        LEFT JOIN delivery_tasks dt ON dt.task_id = dto.task_id
        """;

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
            ORDER_SELECT + " ORDER BY o.created_at DESC, o.order_id DESC",
            (resultSet, rowNumber) -> mapOrder(resultSet)
        );
    }

    public List<OrderResponse> getOrdersForCustomer(long customerId) {
        return jdbcTemplate.query(
            ORDER_SELECT + " WHERE o.customer_id = ? ORDER BY o.created_at DESC, o.order_id DESC",
            (resultSet, rowNumber) -> mapOrder(resultSet),
            customerId
        );
    }

    public Optional<OrderResponse> getOrder(long orderId) {
        return jdbcTemplate.query(ORDER_SELECT + " WHERE o.order_id = ?", (resultSet, rowNumber) -> mapOrder(resultSet), orderId)
            .stream().findFirst();
    }

    public Optional<OrderResponse> getOrderByStripeSession(String sessionId) {
        return jdbcTemplate.query(ORDER_SELECT + " WHERE o.stripe_session_id = ?", (resultSet, rowNumber) -> mapOrder(resultSet), sessionId)
            .stream().findFirst();
    }

    private OrderResponse mapOrder(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        long orderId = resultSet.getLong("order_id");

        return new OrderResponse(
            orderId,
            resultSet.getObject("customer_id", Long.class),
            resultSet.getLong("cart_id"),
            resultSet.getString("order_status"),
            resultSet.getString("delivery_address"),
            resultSet.getBigDecimal("subtotal"),
            resultSet.getBigDecimal("delivery_fee"),
            resultSet.getBigDecimal("total_weight"),
            getOrderItems(orderId),
            resultSet.getObject("created_ms", Long.class),
            resultSet.getObject("dispatch_ms", Long.class),
            resultSet.getObject("arrival_ms", Long.class)
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

    // ---- checkout ----

    public long createPendingOrder(long customerId, long cartId, String deliveryAddress,
                                   BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal totalWeight) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO orders (customer_id, cart_id, order_status, delivery_address, subtotal, delivery_fee, total_weight) " +
                "VALUES (?, ?, 'Pending', ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, customerId);
            statement.setLong(2, cartId);
            statement.setString(3, deliveryAddress);
            statement.setBigDecimal(4, subtotal);
            statement.setBigDecimal(5, deliveryFee);
            statement.setBigDecimal(6, totalWeight);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void addOrderItem(long orderId, long productId, String productName, BigDecimal unitWeight, BigDecimal unitPrice, int quantity) {
        jdbcTemplate.update(
            "INSERT INTO order_items (order_id, product_id, product_name, unit_weight, unit_price, quantity) VALUES (?, ?, ?, ?, ?, ?)",
            orderId, productId, productName, unitWeight, unitPrice, quantity
        );
    }

    public void setStripeSession(long orderId, String sessionId) {
        jdbcTemplate.update("UPDATE orders SET stripe_session_id = ? WHERE order_id = ?", sessionId, orderId);
    }

    /** Pending -> Ordered. Returns 0 if the order was not Pending, which makes payment confirmation idempotent. */
    public int markPaid(long orderId, String paymentIntentId) {
        return jdbcTemplate.update(
            "UPDATE orders SET order_status = 'Ordered', payment_intent_id = ?, paid_at = NOW() WHERE order_id = ? AND order_status = 'Pending'",
            paymentIntentId, orderId
        );
    }

    public Long getPendingOrderIdForCart(long cartId) {
        List<Long> ids = jdbcTemplate.queryForList(
            "SELECT order_id FROM orders WHERE cart_id = ? AND order_status = 'Pending'", Long.class, cartId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public String getStripeSessionId(long orderId) {
        List<String> ids = jdbcTemplate.queryForList("SELECT stripe_session_id FROM orders WHERE order_id = ?", String.class, orderId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public String getPaymentIntentId(long orderId) {
        List<String> ids = jdbcTemplate.queryForList("SELECT payment_intent_id FROM orders WHERE order_id = ?", String.class, orderId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    // Puts the order's items back on the shelf (unpaid checkout released, or order cancelled).
    public void restock(long orderId) {
        jdbcTemplate.update(
            "UPDATE products p JOIN order_items oi ON oi.product_id = p.product_id " +
            "SET p.quantity = p.quantity + oi.quantity WHERE oi.order_id = ?",
            orderId
        );
    }

    public void deleteOrder(long orderId) {
        jdbcTemplate.update("DELETE FROM orders WHERE order_id = ?", orderId);
    }

    // Pending orders older than the Stripe session lifetime; their stock reservation should be released.
    public List<Long> getStalePendingOrderIds(int minutes) {
        return jdbcTemplate.queryForList(
            "SELECT order_id FROM orders WHERE order_status = 'Pending' AND created_at < NOW() - INTERVAL ? MINUTE",
            Long.class, minutes
        );
    }
}
