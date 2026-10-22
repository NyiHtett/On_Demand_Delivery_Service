package com.example.demo.controller;

import com.example.demo.dto.CartResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.CartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public CartResponse getCurrentCart(@AuthenticationPrincipal UserResponse user) {
        return cartService.getCurrentCart(user.id());
    }
}
