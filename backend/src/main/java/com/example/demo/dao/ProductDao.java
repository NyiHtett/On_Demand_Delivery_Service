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

    public int updateProduct(long productId, int quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal unitWeight, String description, String imageUrl) {
        return jdbcTemplate.update(
            "UPDATE products SET quantity = ?, unit_price = ?, unit_weight = ?, product_description = ?, image_url = ? WHERE product_id = ?",
            quantity,
            unitPrice,
            unitWeight,
            description,
            imageUrl,
            productId
        );
    }

    public long createProduct(String name, String description, java.math.BigDecimal unitWeight, java.math.BigDecimal unitPrice, String imageURL, int quantity) {
        jdbcTemplate.update(
            "INSERT INTO products (product_name, product_description, unit_weight, unit_price, image_url, quantity) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            name, description, unitWeight, unitPrice, imageURL, quantity
        );

        return jdbcTemplate.queryForObject(
            "SELECT product_id FROM products WHERE product_name = ?",
            Long.class,
            name
        );
    }
}
