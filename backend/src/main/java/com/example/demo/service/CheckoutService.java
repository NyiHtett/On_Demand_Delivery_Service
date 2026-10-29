package com.example.demo.service;

import com.example.demo.dao.CartDao;
import com.example.demo.dao.OrderDao;
import com.example.demo.dao.ProductDao;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.CheckoutSessionResponse;
import com.example.demo.dto.ConfirmCheckoutRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cart -> order -> payment.
 *
 * 1. createSession: inside one transaction, re-check stock with row locks, snapshot the cart into a
 *    Pending order and take the items off the shelf. Then open a Stripe Checkout session for it.
 * 2. confirm: Stripe sends the customer back with the session id; if Stripe says it's paid the order
 *    becomes Ordered, the cart is closed, and the order joins the robot queue.
 * 3. cancel / releaseStalePendingOrders: unpaid orders are deleted and their stock put back.
 * Without STRIPE_SECRET_KEY the order is marked paid immediately so the app still works locally.
 */
@Service
public class CheckoutService {
    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);
    // A little longer than the Stripe session lifetime, so we never release an order that can still be paid.
    private static final int PENDING_ORDER_MINUTES = (int) StripePaymentService.SESSION_MINUTES + 5;

    private final CartDao cartDao;
    private final ProductDao productDao;
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final StripePaymentService stripe;
    private final DeliveryQueueService deliveryQueue;
    private final TransactionTemplate transactionTemplate;
    private final String frontendUrl;

    public CheckoutService(
        CartDao cartDao,
        ProductDao productDao,
        OrderDao orderDao,
        UserDao userDao,
        StripePaymentService stripe,
        DeliveryQueueService deliveryQueue,
        PlatformTransactionManager transactionManager,
        @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl
    ) {
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.stripe = stripe;
        this.deliveryQueue = deliveryQueue;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.frontendUrl = frontendUrl.replaceAll("/$", "");
        log.info("Stripe checkout {}", stripe.isEnabled() ? "enabled" : "disabled (no STRIPE_SECRET_KEY): orders are marked paid immediately");
    }

    public CheckoutSessionResponse createSession(long userId, String requestedAddress) {
        String address = requestedAddress == null || requestedAddress.isBlank()
            ? userDao.getCurrentAccountByUserId(userId).map(AccountResponse::address).orElse(null)
            : requestedAddress.trim();
        if (address == null || address.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a delivery address.");
        }

        long orderId = transactionTemplate.execute(status -> placePendingOrder(userId, address));

        if (!stripe.isEnabled()) {
            markPaid(orderId, null);
            return new CheckoutSessionResponse(orderId, frontendUrl + "/order-confirmation?order_id=" + orderId);
        }

        try {
            OrderResponse order = orderDao.getOrder(orderId).orElseThrow();
            Session session = stripe.createCheckoutSession(
                order,
                frontendUrl + "/order-confirmation?session_id={CHECKOUT_SESSION_ID}",
                frontendUrl + "/checkout?canceled=1&session_id={CHECKOUT_SESSION_ID}"
            );
            orderDao.setStripeSession(orderId, session.getId());
            return new CheckoutSessionResponse(orderId, session.getUrl());
        } catch (StripeException exception) {
            releasePendingOrder(orderId);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Stripe couldn't start checkout: " + exception.getMessage());
        }
    }

    private long placePendingOrder(long userId, String address) {
        if (!cartDao.containsActiveCart(userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your cart is empty.");
        }
        long cartId = cartDao.getUserCartID(userId);
        List<CartItemResponse> items = cartDao.getCartItems(cartId);
        if (items.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your cart is empty.");
        }

        // Retrying checkout (e.g. after backing out of Stripe) replaces the earlier unpaid order.
        Long previous = orderDao.getPendingOrderIdForCart(cartId);
        if (previous != null) {
            expireStripeSession(previous);
            orderDao.restock(previous);
            orderDao.deleteOrder(previous);
        }

        // Requirement 3e: re-check availability, with the rows locked until this transaction ends.
        List<Long> productIds = items.stream().map(CartItemResponse::productID).filter(id -> id != null).toList();
        Map<Long, ProductResponse> products = productDao.lockProducts(productIds).stream()
            .collect(Collectors.toMap(ProductResponse::productId, Function.identity()));

        List<String> problems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal weight = BigDecimal.ZERO;
        for (CartItemResponse item : items) {
            ProductResponse product = item.productID() == null ? null : products.get(item.productID());
            if (product == null) {
                problems.add(item.productName() + " is no longer sold");
            } else if (product.quantity() < item.quantity()) {
                problems.add(product.quantity() == 0
                    ? product.name() + " is sold out"
                    : "only " + product.quantity() + " " + product.name() + " left");
            } else {
                subtotal = subtotal.add(product.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
                weight = weight.add(product.unitWeight().multiply(BigDecimal.valueOf(item.quantity())));
            }
        }
        if (!problems.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Some items changed while you were shopping: " + String.join("; ", problems) + ". Update your cart and try again.");
        }
        if (weight.compareTo(deliveryQueue.getMaxWeight()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Orders over " + deliveryQueue.getMaxWeight() + " lb are too heavy for a delivery robot.");
        }

        long orderId = orderDao.createPendingOrder(userId, cartId, address,
            subtotal.setScale(2, RoundingMode.HALF_UP),
            CartService.deliveryFeeFor(weight).setScale(2, RoundingMode.HALF_UP),
            weight.setScale(3, RoundingMode.HALF_UP));

        for (CartItemResponse item : items) {
            ProductResponse product = products.get(item.productID());
            orderDao.addOrderItem(orderId, product.productId(), product.name(), product.unitWeight(), product.unitPrice(), item.quantity());
            productDao.decrementStock(product.productId(), item.quantity());
        }
        return orderId;
    }

    public OrderResponse confirm(long userId, ConfirmCheckoutRequest req) {
        if (req == null || ((req.sessionId() == null || req.sessionId().isBlank()) && req.orderId() == null)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "sessionId or orderId is required.");
        }

        OrderResponse order = req.sessionId() != null && !req.sessionId().isBlank()
            ? orderDao.getOrderByStripeSession(req.sessionId()).orElse(null)
            : orderDao.getOrder(req.orderId()).orElse(null);
        if (order == null || order.customerId() == null || order.customerId() != userId) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Order not found.");
        }

        if ("Pending".equals(order.orderStatus()) && req.sessionId() != null && stripe.isEnabled()) {
            try {
                Session session = stripe.retrieveSession(req.sessionId());
                if (!"paid".equals(session.getPaymentStatus())) {
                    throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Payment hasn't gone through yet.");
                }
                markPaid(order.orderId(), session.getPaymentIntent());
            } catch (StripeException exception) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Couldn't check the payment with Stripe: " + exception.getMessage());
            }
        }
        return orderDao.getOrder(order.orderId()).orElseThrow();
    }

    /** Customer backed out of the Stripe page: put the items back and drop the unpaid order. The cart is untouched. */
    public void cancel(long userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;
        orderDao.getOrderByStripeSession(sessionId)
            .filter(order -> order.customerId() != null && order.customerId() == userId)
            .filter(order -> "Pending".equals(order.orderStatus()))
            .ifPresent(order -> {
                expireStripeSession(order.orderId());
                releasePendingOrder(order.orderId());
            });
    }

    /** Pending -> Ordered, close the cart, and queue the order for a robot. Safe to call twice. */
    void markPaid(long orderId, String paymentIntentId) {
        transactionTemplate.executeWithoutResult(status -> {
            if (orderDao.markPaid(orderId, paymentIntentId) == 0) return;
            OrderResponse order = orderDao.getOrder(orderId).orElseThrow();
            cartDao.setCartStatus(order.cartId(), "Ordered");
            deliveryQueue.enqueue(orderId, order.totalWeight());
        });
    }

    private void releasePendingOrder(long orderId) {
        transactionTemplate.executeWithoutResult(status -> {
            if (!"Pending".equals(orderDao.getOrderStatus(orderId))) return;
            orderDao.restock(orderId);
            orderDao.deleteOrder(orderId);
        });
    }

    private void expireStripeSession(long orderId) {
        String sessionId = orderDao.getStripeSessionId(orderId);
        if (sessionId != null && stripe.isEnabled()) stripe.expireSession(sessionId);
    }

    /** Abandoned checkouts: release their stock, unless Stripe says they were paid after all (customer closed the tab). */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void releaseStalePendingOrders() {
        for (long orderId : orderDao.getStalePendingOrderIds(PENDING_ORDER_MINUTES)) {
            try {
                String sessionId = orderDao.getStripeSessionId(orderId);
                if (sessionId != null && stripe.isEnabled()) {
                    Session session = stripe.retrieveSession(sessionId);
                    if ("paid".equals(session.getPaymentStatus())) {
                        markPaid(orderId, session.getPaymentIntent());
                        continue;
                    }
                    stripe.expireSession(sessionId);
                }
                releasePendingOrder(orderId);
                log.info("Released unpaid order {}", orderId);
            } catch (StripeException | RuntimeException exception) {
                log.warn("Couldn't release unpaid order {}: {}", orderId, exception.getMessage());
            }
        }
    }
}
