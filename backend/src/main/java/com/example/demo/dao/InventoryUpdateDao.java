package com.example.demo.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryUpdateDao {
    private final JdbcTemplate jdbcTemplate;

    public InventoryUpdateDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void inventoryUpdate(long productId, String productName, Long employeeId, String employeeName, String action, String fieldName, String oldValue, String newValue, String description) {
        jdbcTemplate.update(
            "INSERT INTO inventory_updates (product_id, product_name, employee_id, employee_name, inventory_updates_action, field_name, old_value, new_value, inventory_updates_description) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            productId, productName, employeeId, employeeName, action, fieldName, oldValue, newValue, description
        );
    }
}