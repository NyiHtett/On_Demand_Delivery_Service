package com.example.demo.dao;

import com.example.demo.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Responsible for interacting with the database to perform CRUD operations on Customer entities.
 */
@Repository
public class CustomerDao {
    private final JdbcTemplate jdbcTemplate;

    public CustomerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // return a list of customers
    public List<Customer> getAllCustomers() {
        // sql statement 
        // DESCRIBE the table to know the columns
        String sql = """
            SELECT customer_id, name, email, address, phone, created_at
            FROM customers
            ORDER BY customer_id
        """;
    
    // result and rowNumber are parameters given by the template
    return jdbcTemplate.query(sql, (resultSet, rowNumber) ->
        new Customer(
            resultSet.getLong("customer_id"), 
            resultSet.getString("name"), 
            resultSet.getString("email"), 
            resultSet.getString("address"), 
            resultSet.getString("phone"), 
            resultSet.getTimestamp("created_at").toLocalDateTime()
        )
    );

    }
}