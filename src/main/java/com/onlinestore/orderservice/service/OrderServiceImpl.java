package com.onlinestore.orderservice.service;

import com.onlinestore.orderservice.dto.CreateOrderItemRequest;
import com.onlinestore.orderservice.dto.CreateOrderRequest;
import com.onlinestore.orderservice.dto.OrderCreatedEvent;
import com.onlinestore.orderservice.dto.OrderDTO;
import com.onlinestore.orderservice.dto.OrderItemDTO;
import com.onlinestore.orderservice.entity.Order;
import com.onlinestore.orderservice.entity.OrderItem;
import com.onlinestore.orderservice.entity.OrderStatus;
import com.onlinestore.orderservice.exception.InsufficientStockException;
import com.onlinestore.orderservice.exception.ResourceNotFoundException;
import com.onlinestore.orderservice.publisher.OrderEventPublisher;
import com.onlinestore.orderservice.repository.OrderRepository;
import com.onlinestore.productservice.dto.ProductDTO;
import com.onlinestore.productservice.service.ProductService;
import com.onlinestore.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional
    public OrderDTO createOrder(String userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);

        // Auto-create the user profile if it doesn't exist yet (first interaction).
        // This prevents ResourceNotFoundException when a user places an order before
        // visiting the /me endpoint.
        var user = userService.getCurrentUser(userId, null, null, null);

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderStatus(OrderStatus.PENDING);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            ProductDTO product = productService.getProductById(itemRequest.getProductId());

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product id: " + product.getId());
            }

            productService.updateStock(product.getId(), -itemRequest.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            
            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        
        OrderDTO orderDTO = mapToOrderDTO(savedOrder);
        
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(userId)
                .email(user.getEmail())
                .totalAmount(totalAmount)
                .items(orderDTO.getOrderItems())
                .timestamp(LocalDateTime.now())
                .build();
                
        orderEventPublisher.publishOrderCreatedEvent(event);
        
        return orderDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id, String userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found or does not belong to user"));
        return mapToOrderDTO(order);
    }

    private OrderDTO mapToOrderDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .orderItems(itemDTOs)
                .build();
    }
}
