package com.example.demo.controller;

import com.example.demo.dto.PaymentMethodResponse;
import com.example.demo.dto.PaymentMethodSetupRequest;
import com.example.demo.dto.PaymentMethodSetupResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.PaymentMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@PreAuthorize("hasRole('CUSTOMER')")
public class PaymentController {
    private final PaymentMethodService paymentMethodService;

    public PaymentController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public List<PaymentMethodResponse> getPaymentMethods(
            @AuthenticationPrincipal UserResponse user
    ) {
        return paymentMethodService.getPaymentMethods(user.id());
    }

    @PostMapping("/setup-session")
    public PaymentMethodSetupResponse createSetupSession(
            @AuthenticationPrincipal UserResponse user
    ) {
        return paymentMethodService.createSetupSession(user.id());
    }

    @PostMapping("/setup-session/confirm")
    public PaymentMethodResponse confirmSetupSession(
            @AuthenticationPrincipal UserResponse user,
            @RequestBody PaymentMethodSetupRequest request
    ) {
        return paymentMethodService.confirmSetupSession(user.id(), request.sessionId());
    }

    @DeleteMapping("/{paymentMethodId}")
    public ResponseEntity<Void> deletePaymentMethod(
            @AuthenticationPrincipal UserResponse user,
            @PathVariable Long paymentMethodId
    ) {
        paymentMethodService.deletePaymentMethod(user.id(), paymentMethodId);
        return ResponseEntity.noContent().build();
    }
}
