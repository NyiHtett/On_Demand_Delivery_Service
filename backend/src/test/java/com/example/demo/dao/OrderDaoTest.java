package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.demo.dto.OrderItemResponse;
import com.example.demo.dto.OrderResponse;
import java.math.BigDecimal;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderDao.class)
public class OrderDaoTest {
    @Autowired
    OrderDao orderDao;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void getOrderStatusReturnsCurrentStatus() {
        jdbcTemplate.update("UPDATE orders SET order_status='Ordered' WHERE order_id=1");

        String status = orderDao.getOrderStatus(1L);

        assertEquals("Ordered", status);
    }

    @Test 
    public void getOrderStatusMissingOrderReturnsNul() {
        String status = orderDao.getOrderStatus(100L);

        assertNull(status);
    }

    @Test
    public void updateOrderStatusChangesRow() {
        jdbcTemplate.update("UPDATE orders SET order_status = 'Active' where order_id = 1");

        int rowsChanged = orderDao.updateOrderStatus(1L, "Cancelled");

        assertEquals(1, rowsChanged);

        String status = jdbcTemplate.queryForObject(
            "SELECT order_status FROM orders WHERE order_id = 1",
            String.class
        );

        assertEquals("Cancelled", status);
    }

    @Test
    public void updateOrderStatusNullOrderReturnsZero() {
        int rowsChanged = orderDao.updateOrderStatus(100L, "Cancelled");

        assertEquals(0, rowsChanged);
    }

    //this test is based off of the single order item that was seeded
    @Test
    public void getAllOrdersSeeded() {
        List<OrderResponse> orders = orderDao.getAllOrders();

        OrderResponse order = orders.stream().filter(ord -> ord.orderId().equals(1L)).findFirst().orElseThrow();

        assertEquals(0, new BigDecimal("22.03").compareTo(order.subtotal()));
        assertEquals(0, new BigDecimal("13.601").compareTo(order.totalWeight()));

        assertEquals(3, order.items().size());

        //will try to test the apples here, just one item is fine
        OrderItemResponse apple = order.items().stream().filter(item -> item.productName().equals("Apple")).findFirst().orElseThrow();
        assertEquals(5, apple.quantity());
        assertEquals(0, new BigDecimal("1.00").compareTo(apple.unitPrice()));
    }
}
