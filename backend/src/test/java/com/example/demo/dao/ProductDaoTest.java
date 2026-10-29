package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.demo.dto.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProductDao.class)
public class ProductDaoTest {
    @Autowired
    ProductDao productDao;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private long createTestProduct() {
        return productDao.createProduct(
            "Test Product Name",
            "used for dao testing",
            new BigDecimal("0.045"),
            new BigDecimal("0.12"),
            null,
            23
        );
    }

    @Test
    public void createProductInsertsRowReturnsId() {
        long newId = createTestProduct();

        String name = jdbcTemplate.queryForObject(
            "SELECT product_name FROM products WHERE product_id = ?", String.class, newId
        );
        Integer quantity = jdbcTemplate.queryForObject(
            "SELECT quantity FROM products WHERE product_id = ?", Integer.class, newId
        );

        assertEquals("Test Product Name", name);
        assertEquals(23, quantity);
    }

    @Test
    public void getProductMissingReturnsEmpty() {
        Optional<ProductResponse> result = productDao.getProduct(100L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void updateProductChangesFields() {
        long id = createTestProduct();

        int rowsChanged = productDao.updateProduct(
            id,
            2,
            new BigDecimal("1.00"),
            new BigDecimal("1.000"),
            "test update",
            null
        );

        assertEquals(1, rowsChanged);

        Integer quantity = jdbcTemplate.queryForObject(
            "SELECT quantity FROM products WHERE product_id = ?", Integer.class, id
        );
        BigDecimal price = jdbcTemplate.queryForObject(
            "SELECT unit_price FROM products WHERE product_id = ?", BigDecimal.class, id
        );
        String description = jdbcTemplate.queryForObject(
            "SELECT product_description FROM products WHERE product_id = ?", String.class, id
        );

        assertEquals(25, quantity);
        assertEquals(0, new BigDecimal("1.00").compareTo(price));
        assertEquals("test update", description);
    }

    @Test
    public void deleteProductRemovesRow() {
        long id = createTestProduct();

        int rowsDeleted = productDao.deleteProduct(id);

        assertEquals(1, rowsDeleted);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM products WHERE product_id = ?", Integer.class, id
        );
        assertEquals(0, count);
    }

    @Test
    public void getAllProductsIncludesProducts() {
        long id = createTestProduct();

        List<ProductResponse> products = productDao.getAllProducts();

        ProductResponse testProduct = products.stream().filter(p -> p.productId().equals(id)).findFirst().orElseThrow();  

        assertEquals("Test Product Name", testProduct.name());
        assertEquals("used for dao testing", testProduct.description());
        assertEquals(0, new BigDecimal("0.045").compareTo(testProduct.unitWeight()));
        assertEquals(0, new BigDecimal("0.12").compareTo(testProduct.unitPrice()));
        assertEquals(23, testProduct.quantity());

        assertTrue(products.stream().anyMatch(p -> p.name().equals("Apple")));
    }

    @Test
    public void updateProductCantBeNegative() {
        long id = createTestProduct();
        
        int rowsChanged = productDao.updateProduct(
            id,
            -1000,
            new BigDecimal("1.00"),
            new BigDecimal("1.000"),
            "update messsage testing testing",
            null
        );

        assertEquals(0, rowsChanged);

        Integer quantity = jdbcTemplate.queryForObject(
            "SELECT quantity FROM products WHERE product_id = ?", Integer.class, id
        );
        assertEquals(23, quantity);
    }
}