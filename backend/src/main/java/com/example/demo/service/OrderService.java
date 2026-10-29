package com.example.demo.service;

import com.example.demo.dao.OrderDao;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.UpdateOrderStatusRequest;
import com.example.demo.exception.ApiException;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final OrderDao orderDao;
    private final DeliveryQueueService deliveryQueue;
    private final StripePaymentService stripe;

    private static final List<String> STATUS_LIST = List.of("Pending", "Ordered", "Cancelled", "Shipping", "Completed");

    private static final List<String> STATUS_LOCK = List.of("Shipping", "Completed", "Cancelled");

    public OrderService(OrderDao orderDao, DeliveryQueueService deliveryQueue, StripePaymentService stripe) {
        this.orderDao = orderDao;
        this.deliveryQueue = deliveryQueue;
        this.stripe = stripe;
    }

    public List<OrderResponse> getAllOrders() {
        return orderDao.getAllOrders();
    }

    public List<OrderResponse> getOrdersForCustomer(long customerId) {
        return orderDao.getOrdersForCustomer(customerId);
    }

    public OrderResponse getOrderForCustomer(long customerId, long orderId) {
        return orderDao.getOrder(orderId)
            .filter(order -> order.customerId() != null && order.customerId() == customerId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));
    }

    @Transactional
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
            throw new ApiException(HttpStatus.BAD_REQUEST, "can't change status for orders that are shipping, completed, or cancelled"); //will protect in the frontend as well, but this is for safety
        }

        if (newStatus.equals("Cancelled")) {
            cancel(orderId, currentStatus);
            return;
        }

        orderDao.updateOrderStatus(orderId, newStatus);

        // An employee confirming payment by hand still needs the order to reach a robot.
        if (currentStatus.equals("Pending") && newStatus.equals("Ordered")) {
            orderDao.getOrder(orderId).ifPresent(order -> deliveryQueue.enqueue(orderId, order.totalWeight()));
        }
    }

    /** Customers can cancel their own order until the robot leaves with it. */
    @Transactional
    public OrderResponse cancelForCustomer(long customerId, long orderId) {
        OrderResponse order = getOrderForCustomer(customerId, orderId);
        if (!order.orderStatus().equals("Ordered") && !order.orderStatus().equals("Pending")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This order has already left the store and can't be cancelled.");
        }
        cancel(orderId, order.orderStatus());
        return orderDao.getOrder(orderId).orElseThrow();
    }

    private void cancel(long orderId, String currentStatus) {
        deliveryQueue.removeFromQueue(orderId);
        orderDao.updateOrderStatus(orderId, "Cancelled");
        orderDao.restock(orderId);

        boolean needsRefund = currentStatus.equals("Ordered");
        if (needsRefund) {
            refundOrder(orderId);
        }
    }

    private void refundOrder(long orderId) {
        // Orders paid without Stripe (no STRIPE_SECRET_KEY) have no payment to refund.
        String paymentIntentId = orderDao.getPaymentIntentId(orderId);
        if (paymentIntentId == null || !stripe.isEnabled()) {
            return;
        }
        try {
            stripe.refund(paymentIntentId);
        } catch (StripeException exception) {
            // Throwing rolls back the cancellation, so the order isn't marked Cancelled without its refund.
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Stripe refund failed: " + exception.getMessage());
        }
    }
}
