package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.dto.CartItemResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

// runs against the seeded database; each test is rolled back
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CartDao.class)
public class CartDaoTest {
    @Autowired
    CartDao cartDao;

    // seed: cart 1 has 3 apples (product 1) and 2 carrots (product 2)
    @Test
    public void seededCartHasApplesAndCarrots() {
        List<CartItemResponse> items = cartDao.getCartItems(1L);

        assertEquals(2, items.size());
        assertEquals(3, cartDao.getItemQuantity(1L, 1L));
        assertEquals(2, cartDao.getItemQuantity(1L, 2L));
    }

    @Test
    public void upsertUpdatesTheSameRow() {
        cartDao.upsertItem(1L, 3L, 2);
        cartDao.upsertItem(1L, 3L, 5);

        assertEquals(5, cartDao.getItemQuantity(1L, 3L));
        assertEquals(3, cartDao.getCartItems(1L).size());
    }

    @Test
    public void deleteItemRemovesIt() {
        assertEquals(1, cartDao.deleteItem(1L, 1L));

        assertEquals(0, cartDao.getItemQuantity(1L, 1L));
    }
}
