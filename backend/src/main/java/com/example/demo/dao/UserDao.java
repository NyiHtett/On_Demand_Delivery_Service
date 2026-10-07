package com.example.demo.dao;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.UserResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public List<UserResponse> getAllUsers() {
        // sql statement 
        // DESCRIBE the table to know the columns
        String sql = """
            SELECT user_id, user_name AS name, email, user_type
            FROM users
            ORDER BY user_id
        """;
    
        // result and rowNumber are parameters given by the template
        return jdbcTemplate.query(sql, (resultSet, rowNumber) ->
            new UserResponse(
                resultSet.getLong("user_id"), 
                resultSet.getString("name"), 
                resultSet.getString("email"), 
                resultSet.getString("user_type")
            )
        );
    }

    
    public UserResponse signUpUser(String name, String email, String passwordHash) {
        String sql = "INSERT INTO users (user_name, email, password_hash, user_type) VALUES (?, ?, ?, ?)";
        
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
            return new UserResponse(userId, name, email, "Customer");
        }

        return null;
    }

    public UserResponse getUserByEmail(String email) {
        String sql = """
            SELECT user_id, user_name AS name, email, user_type
            FROM users
            WHERE email = ?;
        """;
    
        // result and rowNumber are parameters given by the template
        List <UserResponse> users = jdbcTemplate.query(sql, (resultSet, rowNumber) -> {
            return new UserResponse(
                resultSet.getLong("user_id"), 
                resultSet.getString("name"), 
                resultSet.getString("email"),
                resultSet.getString("user_type")
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

    public String createSession(long userId) {
        String apiToken = UUID.randomUUID().toString();
        jdbcTemplate.update(
            "INSERT INTO sessions (session_id, user_id) VALUES (?, ?)",
            apiToken,
            userId
        );
        return apiToken;
    }

    public Optional<UserResponse> getUserBySessionId(String sessionId) {
        String sql = """
            SELECT u.user_id, u.user_name AS name, u.email, u.user_type
            FROM sessions s
            JOIN users u ON u.user_id = s.user_id
            WHERE s.session_id = ?
        """;

        List<UserResponse> users = jdbcTemplate.query(sql, (resultSet, rowNumber) ->
            new UserResponse(
                resultSet.getLong("user_id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("user_type")
            ),
            sessionId
        );
        return users.stream().findFirst();
    }

    public void deleteSession(String sessionId) {
        jdbcTemplate.update("DELETE FROM sessions WHERE session_id = ?", sessionId);
    }

    public Optional<AccountResponse> getCurrentAccountByUserId(Long user_id) {
        String sql = """
                SELECT
                    u.user_id, 
                    u.user_name, 
                    u.email, 
                    u.phone, 
                    u.address, 
                    u.user_type
                FROM sessions s
                JOIN users u ON u.user_id = s.user_id
                WHERE s.user_id = ?
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> 
            new AccountResponse(
                resultSet.getLong("user_id"),
                resultSet.getString("user_name"),
                resultSet.getString("email"),
                resultSet.getString("address"),
                resultSet.getString("phone"),
                resultSet.getString("user_type")
            ), user_id
    ).stream().findFirst();
        
    }
}
