package com.example.demo.dao;

import com.example.demo.dto.TrackingOrderResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Robot delivery batches (delivery_tasks) and their orders (delivery_task_orders).
 * All time arithmetic happens in SQL with NOW() so it never mixes the DB's and the JVM's time zones;
 * times are returned to callers as epoch milliseconds.
 */
@Repository
public class DeliveryDao {
    private final JdbcTemplate jdbcTemplate;

    public DeliveryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record Batch(long taskId, int orderCount, BigDecimal totalWeight, long openedAt, Long dispatchAt) {}

    public record Run(long taskId, String status, long startTime, Integer durationSeconds, Integer distanceMeters, String encodedPolyline) {}

    private static final String BATCH_SELECT = """
        SELECT task_id, order_count, total_weight,
               CAST(UNIX_TIMESTAMP(assigned_at) * 1000 AS SIGNED) AS opened_ms,
               CAST(UNIX_TIMESTAMP(dispatch_at) * 1000 AS SIGNED) AS dispatch_ms
        FROM delivery_tasks
        """;

    // A batch still accepts orders while its timer hasn't run out (closed batches leave on the next dispatcher tick).
    private static final String OPEN_BATCH_WHERE =
        " WHERE delivery_task_status = 'Not Started' AND (dispatch_at IS NULL OR dispatch_at > NOW())";

    private Batch mapBatch(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Batch(rs.getLong("task_id"), rs.getInt("order_count"), rs.getBigDecimal("total_weight"),
            rs.getLong("opened_ms"), rs.getObject("dispatch_ms", Long.class));
    }

    public Optional<Batch> lockOpenBatch() {
        return jdbcTemplate.query(BATCH_SELECT + OPEN_BATCH_WHERE + " ORDER BY task_id DESC LIMIT 1 FOR UPDATE",
            (rs, rowNum) -> mapBatch(rs)).stream().findFirst();
    }

    public Optional<Batch> getOpenBatch() {
        return jdbcTemplate.query(BATCH_SELECT + OPEN_BATCH_WHERE + " ORDER BY task_id DESC LIMIT 1",
            (rs, rowNum) -> mapBatch(rs)).stream().findFirst();
    }

    public long createBatch(String pickupAddress) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO delivery_tasks (pickup_address) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, pickupAddress);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void addOrderToBatch(long taskId, long orderId, BigDecimal weight) {
        jdbcTemplate.update("INSERT INTO delivery_task_orders (task_id, order_id) VALUES (?, ?)", taskId, orderId);
        jdbcTemplate.update(
            "UPDATE delivery_tasks SET order_count = order_count + 1, total_weight = total_weight + ? WHERE task_id = ?",
            weight, taskId);
    }

    /** Leave secondsAfterOpen after the batch opened, or right away if that moment has already passed. */
    public void setDispatchAfterOpen(long taskId, long secondsAfterOpen) {
        jdbcTemplate.update(
            "UPDATE delivery_tasks SET dispatch_at = GREATEST(NOW(), TIMESTAMPADD(SECOND, ?, assigned_at)) WHERE task_id = ?",
            secondsAfterOpen, taskId);
    }

    public void dispatchNow(long taskId) {
        jdbcTemplate.update("UPDATE delivery_tasks SET dispatch_at = NOW() WHERE task_id = ?", taskId);
    }

    public List<Long> getDueBatchIds() {
        return jdbcTemplate.queryForList(
            "SELECT task_id FROM delivery_tasks WHERE delivery_task_status = 'Not Started' AND order_count > 0 AND dispatch_at <= NOW() ORDER BY task_id",
            Long.class);
    }

    public String getPickupAddress(long taskId) {
        return jdbcTemplate.queryForObject("SELECT pickup_address FROM delivery_tasks WHERE task_id = ?", String.class, taskId);
    }

    public List<TrackingOrderResponse> getTaskOrders(long taskId) {
        return jdbcTemplate.query("""
            SELECT o.order_id, COALESCE(u.user_name, 'Guest customer') AS customer_name, o.delivery_address,
                   o.order_status, o.total_weight, dto.stop_number, dto.stop_latitude, dto.stop_longitude,
                   CAST(UNIX_TIMESTAMP(dto.estimated_arrival) * 1000 AS SIGNED) AS arrival_ms
            FROM delivery_task_orders dto
            JOIN orders o ON o.order_id = dto.order_id
            LEFT JOIN users u ON u.user_id = o.customer_id
            WHERE dto.task_id = ?
            ORDER BY COALESCE(dto.stop_number, 999), o.order_id
            """, (rs, rowNum) -> new TrackingOrderResponse(
                rs.getLong("order_id"),
                rs.getString("customer_name"),
                rs.getString("delivery_address"),
                rs.getString("order_status"),
                rs.getBigDecimal("total_weight"),
                rs.getObject("stop_number", Integer.class),
                rs.getObject("stop_latitude", Double.class),
                rs.getObject("stop_longitude", Double.class),
                rs.getObject("arrival_ms", Long.class)
            ), taskId);
    }

    /** Not Started -> En Route. Returns 0 if another tick already dispatched it. */
    public int startTask(long taskId, Integer durationSeconds, Integer distanceMeters, String encodedPolyline) {
        return jdbcTemplate.update(
            "UPDATE delivery_tasks SET delivery_task_status = 'En Route', start_time = NOW(), duration_seconds = ?, " +
            "distance_meters = ?, encoded_polyline = ? WHERE task_id = ? AND delivery_task_status = 'Not Started'",
            durationSeconds, distanceMeters, encodedPolyline, taskId);
    }

    public void setStop(long taskId, long orderId, int stopNumber, Double latitude, Double longitude, long secondsAfterStart) {
        jdbcTemplate.update(
            "UPDATE delivery_task_orders dto JOIN delivery_tasks dt ON dt.task_id = dto.task_id " +
            "SET dto.stop_number = ?, dto.stop_latitude = ?, dto.stop_longitude = ?, " +
            "dto.estimated_arrival = TIMESTAMPADD(SECOND, ?, dt.start_time) WHERE dto.task_id = ? AND dto.order_id = ?",
            stopNumber, latitude, longitude, secondsAfterStart, taskId, orderId);
    }

    public void markTaskOrdersShipping(long taskId) {
        jdbcTemplate.update(
            "UPDATE orders o JOIN delivery_task_orders dto ON dto.order_id = o.order_id " +
            "SET o.order_status = 'Shipping' WHERE dto.task_id = ? AND o.order_status = 'Ordered'",
            taskId);
    }

    /** Shipping -> Completed for every stop the robot has reached. */
    public int completeArrivedOrders() {
        return jdbcTemplate.update(
            "UPDATE orders o JOIN delivery_task_orders dto ON dto.order_id = o.order_id " +
            "JOIN delivery_tasks dt ON dt.task_id = dto.task_id " +
            "SET o.order_status = 'Completed' " +
            "WHERE dt.delivery_task_status = 'En Route' AND o.order_status = 'Shipping' AND dto.estimated_arrival <= NOW()");
    }

    /** En Route -> Completed once the route time is up and no order on it is still Shipping. */
    public int completeFinishedTasks() {
        return jdbcTemplate.update(
            "UPDATE delivery_tasks dt SET dt.delivery_task_status = 'Completed', dt.end_time = NOW() " +
            "WHERE dt.delivery_task_status = 'En Route' " +
            "AND TIMESTAMPADD(SECOND, COALESCE(dt.duration_seconds, 0), dt.start_time) <= NOW() " +
            "AND NOT EXISTS (SELECT 1 FROM delivery_task_orders dto JOIN orders o ON o.order_id = dto.order_id " +
            "                WHERE dto.task_id = dt.task_id AND o.order_status = 'Shipping')");
    }

    /**
     * Takes a cancelled order off a batch that hasn't left yet. Returns the batch's remaining order count,
     * or null if the order wasn't waiting in a batch.
     */
    public Integer removeFromWaitingBatch(long orderId) {
        List<Long> taskIds = jdbcTemplate.queryForList(
            "SELECT dt.task_id FROM delivery_task_orders dto JOIN delivery_tasks dt ON dt.task_id = dto.task_id " +
            "WHERE dto.order_id = ? AND dt.delivery_task_status = 'Not Started' FOR UPDATE",
            Long.class, orderId);
        if (taskIds.isEmpty()) return null;
        long taskId = taskIds.get(0);
        jdbcTemplate.update(
            "UPDATE delivery_tasks dt JOIN orders o ON o.order_id = ? " +
            "SET dt.order_count = dt.order_count - 1, dt.total_weight = dt.total_weight - o.total_weight WHERE dt.task_id = ?",
            orderId, taskId);
        jdbcTemplate.update("DELETE FROM delivery_task_orders WHERE order_id = ?", orderId);
        jdbcTemplate.update("DELETE FROM delivery_tasks WHERE task_id = ? AND order_count <= 0", taskId);
        return jdbcTemplate.queryForList("SELECT order_count FROM delivery_tasks WHERE task_id = ?", Integer.class, taskId)
            .stream().findFirst().orElse(0);
    }

    public Long getWaitingBatchId(long orderId) {
        List<Long> taskIds = jdbcTemplate.queryForList(
            "SELECT dt.task_id FROM delivery_task_orders dto JOIN delivery_tasks dt ON dt.task_id = dto.task_id " +
            "WHERE dto.order_id = ? AND dt.delivery_task_status = 'Not Started'",
            Long.class, orderId);
        return taskIds.isEmpty() ? null : taskIds.get(0);
    }

    public List<Run> getRobotsOnTheRoad() {
        return jdbcTemplate.query("""
            SELECT task_id, delivery_task_status, CAST(UNIX_TIMESTAMP(start_time) * 1000 AS SIGNED) AS start_ms,
                   duration_seconds, distance_meters, encoded_polyline
            FROM delivery_tasks WHERE delivery_task_status = 'En Route' ORDER BY start_time
            """, (rs, rowNum) -> new Run(
                rs.getLong("task_id"),
                rs.getString("delivery_task_status"),
                rs.getLong("start_ms"),
                rs.getObject("duration_seconds", Integer.class),
                rs.getObject("distance_meters", Integer.class),
                rs.getString("encoded_polyline")));
    }
}
