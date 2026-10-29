package com.example.demo.controller;

import com.example.demo.dto.CartItemRequest;
import com.example.demo.dto.CartResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.CartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCurrentCart(@AuthenticationPrincipal UserResponse user) {
        return cartService.getCurrentCart(user.id());
    }

    // body: { "productId": 1, "quantity": 2 } adds 2 more of product 1
    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal UserResponse user, @RequestBody CartItemRequest req) {
        return cartService.addItem(user.id(), req.productId(), req.quantity());
    }

    // body: { "quantity": 3 } sets the quantity (0 removes the item)
    @PutMapping("/items/{productId}")
    public CartResponse updateItem(@AuthenticationPrincipal UserResponse user, @PathVariable long productId, @RequestBody CartItemRequest req) {
        return cartService.updateItem(user.id(), productId, req.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@AuthenticationPrincipal UserResponse user, @PathVariable long productId) {
        return cartService.removeItem(user.id(), productId);
    }
}
