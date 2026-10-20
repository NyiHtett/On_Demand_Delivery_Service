package com.example.demo.controller;

import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.UpdateOrderStatusRequest;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/orders")
@PreAuthorize("hasRole('EMPLOYEE')")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable long orderId, @RequestBody UpdateOrderStatusRequest req) {
        orderService.updateOrderStatus(orderId, req);
        return ResponseEntity.noContent().build();
    }
}