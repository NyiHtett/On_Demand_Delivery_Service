package com.example.demo.dao;

import com.example.demo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Responsible for interacting with the database to perform CRUD operations on User entities.
 */
@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // return a list of Users
    public List<User> getAllUsers() {
        // sql statement 
        // DESCRIBE the table to know the columns
        String sql = """
            SELECT user_id, user_name, email, phone, user_address, password_hash, created_at, updated_at
            FROM Users
            ORDER BY User_id
        """;
    
    // result and rowNumber are parameters given by the template
    return jdbcTemplate.query(sql, (resultSet, rowNumber) ->
        new User(
            resultSet.getLong("user_id"), 
            resultSet.getString("user_name"), 
            resultSet.getString("email"), 
            resultSet.getString("phone"), 
            resultSet.getString("user_address"), 
            resultSet.getString("password_hash"), 
            resultSet.getString("user_type"), 
            resultSet.getTimestamp("created_at").toLocalDateTime(),
            resultSet.getTimestamp("updated_at").toLocalDateTime()
        )
    );

    }
}
