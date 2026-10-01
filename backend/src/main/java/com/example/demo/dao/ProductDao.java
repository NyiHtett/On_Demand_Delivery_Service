package com.example.demo.dao;

import com.example.demo.dto.ProductResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductDao {
    private final JdbcTemplate jdbcTemplate;

    public ProductDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductResponse> getAllProducts() {
        return jdbcTemplate.query(
            baseSelect() + " ORDER BY product_name",
            (resultSet, rowNumber) -> mapProduct(resultSet)
        );
    }

    public Optional<ProductResponse> getProduct(long productId) {
        List<ProductResponse> products = jdbcTemplate.query(
            baseSelect() + " WHERE product_id = ?",
            (resultSet, rowNumber) -> mapProduct(resultSet),
            productId
        );

        return products.stream().findFirst();
    }

    private String baseSelect() {
        return """
            SELECT product_id, product_name, product_description,
                   unit_weight, unit_price, image_url, quantity
            FROM products
            """;
    }

    private ProductResponse mapProduct(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new ProductResponse(
            resultSet.getLong("product_id"),
            resultSet.getString("product_name"),
            resultSet.getString("product_description"),
            resultSet.getBigDecimal("unit_weight"),
            resultSet.getBigDecimal("unit_price"),
            resultSet.getString("image_url"),
            resultSet.getInt("quantity")
        );
    }

    public int updateQuantity(long productId, int quantity) {
        return jdbcTemplate.update(
            "UPDATE products SET quantity = ? WHERE product_id = ?",
            quantity,
            productId
        );
    }
}
