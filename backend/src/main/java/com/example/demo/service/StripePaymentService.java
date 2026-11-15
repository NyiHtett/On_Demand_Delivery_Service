package com.example.demo.service;

import com.example.demo.dto.OrderItemResponse;
import com.example.demo.dto.OrderResponse;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodDetachParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

/** Thin wrapper around Stripe so the rest of the app doesn't touch the SDK directly. */
@Service
public class StripePaymentService {
    // Stripe's minimum Checkout Session lifetime.
    static final long SESSION_MINUTES = 30;

    private final String stripeSecretKey;

    public StripePaymentService(@Value("${stripe.secret-key:}") String stripeSecretKey) {
        this.stripeSecretKey = stripeSecretKey == null ? "" : stripeSecretKey.trim();
    }

    public boolean isEnabled() {
        return !stripeSecretKey.isEmpty();
    }

    private StripeClient client() {
        return new StripeClient(stripeSecretKey);
    }

    public Session createCheckoutSession(OrderResponse order, String successUrl, String cancelUrl) throws StripeException {
        return createCheckoutSession(order, null, successUrl, cancelUrl);
    }

    public Session createCheckoutSession(OrderResponse order, String customerId, String successUrl, String cancelUrl)
            throws StripeException {
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(String.valueOf(order.orderId()))
                .putMetadata("order_id", String.valueOf(order.orderId()))
                .addAllowedPaymentMethodType(SessionCreateParams.AllowedPaymentMethodType.CARD)
                .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                        .setSetupFutureUsage(SessionCreateParams.PaymentIntentData.SetupFutureUsage.OFF_SESSION)
                        .build())
                .setExpiresAt(Instant.now().plusSeconds(SESSION_MINUTES * 60 + 60).getEpochSecond());
        if (customerId != null && !customerId.isBlank()) {
            params.setCustomer(customerId);
        }

        for (OrderItemResponse item : order.items()) {
            params.addLineItem(lineItem(item.productName(), item.unitPrice(), item.quantity()));
        }
        if (order.deliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            params.addLineItem(lineItem("Robot delivery (orders 20 lb and over)", order.deliveryFee(), 1));
        }
        return client().v1().checkout().sessions().create(params.build());
    }

    private static SessionCreateParams.LineItem lineItem(String name, BigDecimal unitPrice, int quantity) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) quantity)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(unitPrice.movePointRight(2).longValueExact())
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(name)
                                                .build())
                                .build())
                .build();
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        return client().v1().checkout().sessions().retrieve(sessionId);
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return client().v1().paymentIntents().retrieve(paymentIntentId);
    }

    public Customer createCustomer(String name, String email) throws StripeException {
        return client().v1().customers().create(CustomerCreateParams.builder()
                .setName(name)
                .setEmail(email)
                .build());
    }

    public Session createPaymentMethodSetupSession(String customerId, String successUrl, String cancelUrl)
            throws StripeException {
        return client().v1().checkout().sessions().create(SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SETUP)
                .setCustomer(customerId)
                .setCurrency("usd")
                .addAllowedPaymentMethodType(SessionCreateParams.AllowedPaymentMethodType.CARD)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .build());
    }

    public SetupIntent retrieveSetupIntent(String setupIntentId) throws StripeException {
        return client().v1().setupIntents().retrieve(setupIntentId);
    }

    public PaymentMethod retrievePaymentMethod(String paymentMethodId) throws StripeException {
        return client().v1().paymentMethods().retrieve(paymentMethodId);
    }

    public void detachPaymentMethod(String paymentMethodId) throws StripeException {
        client().v1().paymentMethods().detach(paymentMethodId, PaymentMethodDetachParams.builder().build());
    }

    /** Stops an unpaid session from being paid later. Errors are ignored (it may already be expired or complete). */
    public void expireSession(String sessionId) {
        try {
            client().v1().checkout().sessions().expire(sessionId);
        } catch (StripeException ignored) {
            // nothing to undo
        }
    }

    public void refund(String paymentIntentId) throws StripeException {
        client().v1().refunds().create(RefundCreateParams.builder().setPaymentIntent(paymentIntentId).build());
    }
}
