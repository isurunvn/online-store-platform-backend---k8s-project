package com.onlinestore.orderservice.controller;

import com.onlinestore.orderservice.dto.CreateOrderRequest;
import com.onlinestore.orderservice.dto.OrderDTO;
import com.onlinestore.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final com.onlinestore.userservice.service.UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderDTO> createOrder(@AuthenticationPrincipal Jwt jwt, 
                                                @Valid @RequestBody CreateOrderRequest request) {
        String userId = jwt.getSubject();
        userService.getCurrentUser(userId, jwt.getClaimAsString("email"), jwt.getClaimAsString("given_name"), jwt.getClaimAsString("family_name"));
        OrderDTO createdOrder = orderService.createOrder(userId, request);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<OrderDTO> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        OrderDTO order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(order);
    }
}
