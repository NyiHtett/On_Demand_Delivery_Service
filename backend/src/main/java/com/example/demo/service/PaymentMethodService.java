package com.example.demo.service;

import com.example.demo.dao.PaymentMethodDao;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.PaymentMethodResponse;
import com.example.demo.dto.PaymentMethodSetupResponse;
import com.example.demo.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

@Service
public class PaymentMethodService {
    private static final Logger log = LoggerFactory.getLogger(PaymentMethodService.class);
    private final PaymentMethodDao paymentMethodDao;
    private final UserDao userDao;
    private final StripePaymentService stripe;
    private final String frontendUrl;

    public PaymentMethodService(
            PaymentMethodDao paymentMethodDao,
            UserDao userDao,
            StripePaymentService stripe,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl
    ) {
        this.paymentMethodDao = paymentMethodDao;
        this.userDao = userDao;
        this.stripe = stripe;
        this.frontendUrl = frontendUrl.replaceAll("/$", "");
    }

    public List<PaymentMethodResponse> getPaymentMethods(long userId) {
        return paymentMethodDao.getByUserId(userId);
    }

    public PaymentMethodSetupResponse createSetupSession(long userId) {
        requireStripe();
        AccountResponse account = getAccount(userId);
        String customerId = getOrCreateCustomer(userId, account);

        try {
            Session session = stripe.createPaymentMethodSetupSession(
                    customerId,
                    frontendUrl + "/account?payment_method_setup=success&session_id={CHECKOUT_SESSION_ID}",
                    frontendUrl + "/account?payment_method_setup=canceled"
            );
            return new PaymentMethodSetupResponse(session.getUrl());
        } catch (StripeException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Stripe couldn't start payment method setup: " + exception.getMessage());
        }
    }

    public PaymentMethodResponse confirmSetupSession(long userId, String sessionId) {
        requireStripe();
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A setup session ID is required.");
        }

        AccountResponse account = getAccount(userId);
        String customerId = userDao.getStripeCustomerId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stripe customer not found."));

        try {
            Session session = stripe.retrieveSession(sessionId);
            if (!customerId.equals(session.getCustomer())) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Payment setup session not found.");
            }

            String setupIntentId = session.getSetupIntent();
            if (setupIntentId == null || setupIntentId.isBlank()) {
                throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Payment method setup has not completed.");
            }

            SetupIntent setupIntent = stripe.retrieveSetupIntent(setupIntentId);
            if (!"succeeded".equals(setupIntent.getStatus())) {
                throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Payment method setup has not completed.");
            }

            String paymentMethodId = setupIntent.getPaymentMethod();
            if (paymentMethodId == null || paymentMethodId.isBlank()) {
                throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Stripe did not return a payment method.");
            }
            PaymentMethod paymentMethod = stripe.retrievePaymentMethod(paymentMethodId);
            if (paymentMethod.getCard() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Only card payment methods are supported.");
            }

            String billingName = paymentMethod.getBillingDetails() == null
                    ? account.user_name()
                    : paymentMethod.getBillingDetails().getName();

            String cardFingerprint = paymentMethod.getCard().getFingerprint();
            if (cardFingerprint == null || cardFingerprint.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "This card can't be saved. It does not have a fingerprint.");
            }
            String previouslyExistingCardID = paymentMethodDao.getPaymentMethodIdByFingerprint(userId, cardFingerprint);
            if (previouslyExistingCardID != null && !previouslyExistingCardID.equals(paymentMethod.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "This card is saved already.");
            }
            boolean saved = savePaymentMethod(
                    userId,
                    customerId,
                    paymentMethod.getId(),
                    paymentMethod.getCard().getBrand(),
                    paymentMethod.getCard().getLast4(),
                    Math.toIntExact(paymentMethod.getCard().getExpMonth()),
                    Math.toIntExact(paymentMethod.getCard().getExpYear()),
                    billingName == null || billingName.isBlank() ? account.user_name() : billingName,
                    cardFingerprint
            );
            if (!saved) {
                throw new ApiException(HttpStatus.CONFLICT, "This card is saved already.");
            }

            return paymentMethodDao.getByUserId(userId).get(0);
        } catch (StripeException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Couldn't confirm payment method with Stripe: " + exception.getMessage());
        }
    }

    public void savePaymentMethodFromPaymentIntent(long userId, String customerId, String paymentIntentId)
            throws StripeException {
        if (paymentIntentId == null || paymentIntentId.isBlank()) return;

        AccountResponse account = getAccount(userId);
        PaymentIntent paymentIntent = stripe.retrievePaymentIntent(paymentIntentId);
        String paymentMethodId = paymentIntent.getPaymentMethod();
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            log.warn("PaymentIntent {} has no payment method", paymentIntentId);
            return;
        }

        PaymentMethod paymentMethod = stripe.retrievePaymentMethod(paymentMethodId);
        log.info("Stripe payment method {} has type {}", paymentMethodId, paymentMethod.getType());
        if (paymentMethod.getCard() == null) {
            log.warn("Stripe payment method {} is not a card; it was not saved", paymentMethodId);
            return;
        }

        String billingName = paymentMethod.getBillingDetails() == null
                ? account.user_name()
                : paymentMethod.getBillingDetails().getName();

        String cardFingerprint = paymentMethod.getCard().getFingerprint();
        if (cardFingerprint == null || cardFingerprint.isBlank()) {
            return; //skip saving because card does not have a fingerprint
        }
        String previouslyExistingCardID = paymentMethodDao.getPaymentMethodIdByFingerprint(userId, cardFingerprint);
        if (previouslyExistingCardID != null && !previouslyExistingCardID.equals(paymentMethod.getId())) {
            return; //skip saving because card is previously saved
        }
        boolean saved = savePaymentMethod(
                userId,
                customerId,
                paymentMethod.getId(),
                paymentMethod.getCard().getBrand(),
                paymentMethod.getCard().getLast4(),
                Math.toIntExact(paymentMethod.getCard().getExpMonth()),
                Math.toIntExact(paymentMethod.getCard().getExpYear()),
                billingName == null || billingName.isBlank() ? account.user_name() : billingName,
                cardFingerprint
        );
        if (!saved) {
            log.warn("Card fingerprint {} is already saved for customer {}; skipping duplicate",
                    cardFingerprint, customerId);
        }
    }

    private boolean savePaymentMethod(long userId, String customerId, String stripePaymentMethodId,
                                      String cardBrand, String lastFour, int expirationMonth,
                                      int expirationYear, String billingName, String cardFingerprint) {
        try {
            paymentMethodDao.save(userId, customerId, stripePaymentMethodId, cardBrand, lastFour,
                    expirationMonth, expirationYear, billingName, cardFingerprint);
            return true;
        } catch (DuplicateKeyException exception) {
            String existingPaymentMethodId =
                    paymentMethodDao.getPaymentMethodIdByFingerprint(userId, cardFingerprint);
            if (stripePaymentMethodId.equals(existingPaymentMethodId)
                    || paymentMethodDao.getIdByStripePaymentMethodId(userId, stripePaymentMethodId) != null) {
                paymentMethodDao.save(userId, customerId, stripePaymentMethodId, cardBrand, lastFour,
                        expirationMonth, expirationYear, billingName, cardFingerprint);
                return true;
            }
            return false;
        }
    }

    public void deletePaymentMethod(long userId, Long paymentMethodId) {
        requireStripe();
        String stripePaymentMethodId = paymentMethodDao.getStripePaymentMethodId(userId, paymentMethodId);
        if (stripePaymentMethodId == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Payment method not found.");
        }

        try {
            stripe.detachPaymentMethod(stripePaymentMethodId);
            paymentMethodDao.deleteByIdAndUserId(paymentMethodId, userId);
        } catch (StripeException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Couldn't remove payment method from Stripe: " + exception.getMessage());
        }
    }

    private AccountResponse getAccount(long userId) {
        return userDao.getCurrentAccountByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found."));
    }

    private String getOrCreateCustomer(long userId, AccountResponse account) {
        return userDao.getStripeCustomerId(userId).orElseGet(() -> {
            try {
                Customer customer = stripe.createCustomer(account.user_name(), account.email());
                userDao.setStripeCustomerId(userId, customer.getId());
                return customer.getId();
            } catch (StripeException exception) {
                throw new ApiException(HttpStatus.BAD_GATEWAY,
                        "Couldn't create Stripe customer: " + exception.getMessage());
            }
        });
    }

    private void requireStripe() {
        if (!stripe.isEnabled()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Stripe is not configured. Add STRIPE_SECRET_KEY and restart the app.");
        }
    }
}
