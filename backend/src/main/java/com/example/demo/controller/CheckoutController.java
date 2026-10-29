package com.example.demo.controller;

import com.example.demo.dto.CheckoutRequest;
import com.example.demo.dto.CheckoutSessionResponse;
import com.example.demo.dto.ConfirmCheckoutRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/checkout")
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // Turns the cart into a Pending order and returns the Stripe Checkout URL to send the customer to.
    @PostMapping("/session")
    public CheckoutSessionResponse createSession(@AuthenticationPrincipal UserResponse user, @RequestBody(required = false) CheckoutRequest req) {
        return checkoutService.createSession(user.id(), req == null ? null : req.deliveryAddress());
    }

    // Called by the confirmation page after Stripe redirects back.
    @PostMapping("/confirm")
    public OrderResponse confirm(@AuthenticationPrincipal UserResponse user, @RequestBody ConfirmCheckoutRequest req) {
        return checkoutService.confirm(user.id(), req);
    }

    // Called by the checkout page when the customer backs out of Stripe.
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserResponse user, @RequestBody ConfirmCheckoutRequest req) {
        checkoutService.cancel(user.id(), req == null ? null : req.sessionId());
        return ResponseEntity.noContent().build();
    }
}
