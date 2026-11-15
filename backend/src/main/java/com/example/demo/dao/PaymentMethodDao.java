package com.example.demo.dao;

import com.example.demo.dto.PaymentMethodResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaymentMethodDao {
    private final JdbcTemplate jdbcTemplate;

    public PaymentMethodDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PaymentMethodResponse> getByUserId(long userId) {
        return jdbcTemplate.query("""
            SELECT id, card_brand, last_four, billing_name,
                   expiration_month, expiration_year, is_default
            FROM stripe_payment_methods
            WHERE user_id = ?
            ORDER BY is_default DESC, created_at DESC, id DESC
            """, (resultSet, rowNumber) -> new PaymentMethodResponse(
                resultSet.getLong("id"),
                resultSet.getString("card_brand"),
                resultSet.getString("last_four"),
                resultSet.getString("billing_name"),
                String.format("%04d-%02d", resultSet.getInt("expiration_year"), resultSet.getInt("expiration_month")),
                resultSet.getBoolean("is_default")
            ), userId);
    }

    public long save(long userId, String stripeCustomerId, String stripePaymentMethodId,
                     String cardBrand, String lastFour, int expirationMonth,
                     int expirationYear, String billingName) {
        // Confirming the same checkout/setup session twice is safe and idempotent.
        jdbcTemplate.update(
            "UPDATE stripe_payment_methods SET is_default = FALSE WHERE user_id = ?",
            userId
        );
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                INSERT INTO stripe_payment_methods
                    (user_id, stripe_customer_id, stripe_payment_method_id, card_brand,
                     last_four, expiration_month, expiration_year, billing_name, is_default)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE)
                ON DUPLICATE KEY UPDATE
                    user_id = VALUES(user_id),
                    stripe_customer_id = VALUES(stripe_customer_id),
                    card_brand = VALUES(card_brand),
                    last_four = VALUES(last_four),
                    expiration_month = VALUES(expiration_month),
                    expiration_year = VALUES(expiration_year),
                    billing_name = VALUES(billing_name),
                    is_default = TRUE
                """, java.sql.Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, userId);
            statement.setString(2, stripeCustomerId);
            statement.setString(3, stripePaymentMethodId);
            statement.setString(4, cardBrand);
            statement.setString(5, lastFour);
            statement.setInt(6, expirationMonth);
            statement.setInt(7, expirationYear);
            statement.setString(8, billingName);
            return statement;
        }, keyHolder);
        Number id = keyHolder.getKey();
        return id == null ? 0 : id.longValue();
    }

    public String getStripePaymentMethodId(long userId, long id) {
        List<String> ids = jdbcTemplate.query(
            "SELECT stripe_payment_method_id FROM stripe_payment_methods WHERE id = ? AND user_id = ?",
            (resultSet, rowNumber) -> resultSet.getString("stripe_payment_method_id"),
            id, userId
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    public int deleteByIdAndUserId(long id, long userId) {
        return jdbcTemplate.update(
            "DELETE FROM stripe_payment_methods WHERE id = ? AND user_id = ?",
            id, userId
        );
    }
}
