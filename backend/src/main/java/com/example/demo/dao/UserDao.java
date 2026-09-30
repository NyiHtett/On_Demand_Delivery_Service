package com.example.demo.dao;

import com.example.demo.dto.UserResponse;
import com.example.demo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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
            SELECT user_id, name, email, phone, address, password_hash, user_type, created_at, updated_at
            FROM users
            ORDER BY User_id
        """;
    
        // result and rowNumber are parameters given by the template
        return jdbcTemplate.query(sql, (resultSet, rowNumber) ->
            new User(
                resultSet.getLong("user_id"), 
                resultSet.getString("name"), 
                resultSet.getString("email"), 
                resultSet.getString("phone"), 
                resultSet.getString("address"), 
                resultSet.getString("password_hash"), 
                resultSet.getString("user_type"), 
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime()
            )
        );
    }

    
    public UserResponse signUpUser(String name, String email, String passwordHash) {
        String sql = "INSERT INTO users (name, email, password_hash, user_type) VALUES (?, ?, ?, ?)";
        
        // KeyHolder is used to capture the auto-generated User ID
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, "Customer");
            return ps;
        }, keyHolder);

        // Retrieve the generated numeric ID safely
        Number key = keyHolder.getKey();
        if (key != null) {
            long userId = key.longValue();
            return new UserResponse(userId, name, email);
        }

        return null;
    }

    public UserResponse getUserByEmail(String email) {
        String sql = """
            SELECT user_id, name, email
            FROM users
            WHERE email = ?;
        """;
    
        // result and rowNumber are parameters given by the template
        List <UserResponse> users = jdbcTemplate.query(sql, (resultSet, rowNumber) -> {
            return new UserResponse(
                resultSet.getLong("user_id"), 
                resultSet.getString("name"), 
                resultSet.getString("email")
            );
        }, email);

        if (users.isEmpty())
            return null;
        else
            return users.get(0);
    }

    public String getPasswordHashByEmail(String email) {
        String sql = "SELECT password_hash FROM users WHERE email = ?";
        List<String> passwordHashes = jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> resultSet.getString("password_hash"),
            email
        );

        return passwordHashes.isEmpty() ? null : passwordHashes.get(0);
    }
}
