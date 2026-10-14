package com.onlinestore.orderservice.service;

import com.onlinestore.orderservice.dto.CreateOrderRequest;
import com.onlinestore.orderservice.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(String userId, CreateOrderRequest request);
    List<OrderDTO> getUserOrders(String userId);
    OrderDTO getOrderById(Long id, String userId);
}
