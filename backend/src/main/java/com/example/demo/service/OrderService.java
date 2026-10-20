package com.example.demo.service;

import com.example.demo.dao.OrderDao;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.UpdateOrderStatusRequest;
import com.example.demo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderDao orderDao;

    private static final List<String> STATUS_LIST = List.of("Active", "Ordered", "Cancelled", "Shipping", "Completed");

    private static final List<String> STATUS_LOCK = List.of("Shipping", "Completed");

    public OrderService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public List<OrderResponse> getAllOrders() {
        return orderDao.getAllOrders();
    }

    public void updateOrderStatus(long orderId, UpdateOrderStatusRequest req) {
        if (req == null || req.orderStatus() == null || req.orderStatus().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "order status can't be empty for update");
        }

        String newStatus = req.orderStatus().trim();
        if (!STATUS_LIST.contains(newStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "status not possible");
        }

        String currentStatus = orderDao.getOrderStatus(orderId);
        if (currentStatus == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "order couldn't be found");
        }

        if (STATUS_LOCK.contains(currentStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "can't change status for orders that are shipping or completed"); //will protect in the frontend as well, but this is for safety
        }
        
        boolean needsRefund = currentStatus.equals("Ordered") && newStatus.equals("Cancelled");

        orderDao.updateOrderStatus(orderId, newStatus);

        if (needsRefund) {
            refundOrder(orderId);
        }
    }

    private void refundOrder(long orderId) {
        //the actual implementation of payments is outside the scope of this project
        //just wanted to simulate the logic when the status changes from ordered to cancelled.
    }
}