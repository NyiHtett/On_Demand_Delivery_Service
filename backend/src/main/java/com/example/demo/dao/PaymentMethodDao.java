package com.example.demo.dao;

import com.example.demo.dto.PaymentMethodResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
            WHERE id IN (
                SELECT MIN(id)
                FROM stripe_payment_methods
                WHERE user_id = ?
                GROUP BY card_fingerprint
            )
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

    @Transactional
    public long save(long userId, String stripeCustomerId, String stripePaymentMethodId,
                     String cardBrand, String lastFour, int expirationMonth,
                     int expirationYear, String billingName, String cardFingerprint) {
        Long existingId = getIdByStripePaymentMethodId(userId, stripePaymentMethodId);
        if (existingId != null) {
            setDefaultPaymentMethod(userId, existingId);
            return existingId;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                INSERT INTO stripe_payment_methods
                    (user_id, stripe_customer_id, stripe_payment_method_id, card_brand,
                     last_four, expiration_month, expiration_year, billing_name, is_default, card_fingerprint)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?)
                """, java.sql.Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, userId);
            statement.setString(2, stripeCustomerId);
            statement.setString(3, stripePaymentMethodId);
            statement.setString(4, cardBrand);
            statement.setString(5, lastFour);
            statement.setInt(6, expirationMonth);
            statement.setInt(7, expirationYear);
            statement.setString(8, billingName);
            statement.setString(9, cardFingerprint);
            return statement;
        }, keyHolder);
        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("Saving the payment method did not return a database ID.");
        }

        setDefaultPaymentMethod(userId, id.longValue());
        return id.longValue();
    }

    private void setDefaultPaymentMethod(long userId, long id) {
        jdbcTemplate.update("""
            UPDATE stripe_payment_methods
            SET is_default = (id = ?)
            WHERE user_id = ?
            """, id, userId);
    }

    public String getStripePaymentMethodId(long userId, long id) {
        List<String> ids = jdbcTemplate.query(
            "SELECT stripe_payment_method_id FROM stripe_payment_methods WHERE id = ? AND user_id = ?",
            (resultSet, rowNumber) -> resultSet.getString("stripe_payment_method_id"),
            id, userId
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    public String getCardFingerprint(long userId, long id) {
        List<String> fingerprints = jdbcTemplate.query(
            "SELECT card_fingerprint FROM stripe_payment_methods WHERE id = ? AND user_id = ?",
            (resultSet, rowNumber) -> resultSet.getString("card_fingerprint"),
            id, userId
        );
        return fingerprints.isEmpty() ? null : fingerprints.get(0);
    }

    public List<String> getStripePaymentMethodIdsByFingerprint(long userId, String fingerprint) {
        return jdbcTemplate.query("""
            SELECT stripe_payment_method_id
            FROM stripe_payment_methods
            WHERE user_id = ? AND card_fingerprint = ?
            """,
            (resultSet, rowNumber) -> resultSet.getString("stripe_payment_method_id"),
            userId, fingerprint
        );
    }

    public Long getIdByStripePaymentMethodId(long userId, String stripePaymentMethodId) {
        List<Long> ids = jdbcTemplate.query(
            "SELECT id FROM stripe_payment_methods WHERE user_id = ? AND stripe_payment_method_id = ?",
            (resultSet, rowNumber) -> resultSet.getLong("id"),
            userId, stripePaymentMethodId
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    public String getPaymentMethodIdByFingerprint(long userId, String fingerprint) {
        List<String> ids = jdbcTemplate.query("""
            SELECT stripe_payment_method_id
            FROM stripe_payment_methods
            WHERE user_id = ? AND card_fingerprint = ?
            LIMIT 1
            """,
            (rs, rowNum) -> rs.getString("stripe_payment_method_id"),
            userId, fingerprint
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    public int deleteByIdAndUserId(long id, long userId) {
        return jdbcTemplate.update(
            "DELETE FROM stripe_payment_methods WHERE id = ? AND user_id = ?",
            id, userId
        );
    }

    public int deleteByFingerprintAndUserId(long userId, String fingerprint) {
        return jdbcTemplate.update(
            "DELETE FROM stripe_payment_methods WHERE user_id = ? AND card_fingerprint = ?",
            userId, fingerprint
        );
    }
}
