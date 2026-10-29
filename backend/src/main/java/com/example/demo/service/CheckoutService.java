package com.example.demo.service;

import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;

@Service
public class CheckoutService {
    private final String stripeSecretKey;

    public CheckoutService(@Value("${stripe.secret-key}") String stripeSecretKey) {
        this.stripeSecretKey = stripeSecretKey;
        System.out.println("Stripe Secret Key: " + !stripeSecretKey.isEmpty());
    }

    public String createSession() throws StripeException { // Hardcoded session to be called
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173" + "/account")
                .setCancelUrl("http://localhost:5173" + "/cart")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(100L)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Test item")
                                                                .build())
                                                .build())
                                .build())
                .build();
        return new StripeClient(stripeSecretKey).v1().checkout().sessions().create(params).getUrl();
    }
}
