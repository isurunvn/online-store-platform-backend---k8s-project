package com.onlinestore.orderservice.publisher;

import com.onlinestore.orderservice.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.order-created}")
    private String routingKey;

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent to exchange: {} with routing key: {}", exchange, routingKey);
        var correlation = new org.springframework.amqp.rabbit.connection.CorrelationData(java.util.UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, routingKey, event, message -> {
            message.getMessageProperties().setDeliveryMode(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
            return message;
        }, correlation);
        try {
            var confirm = correlation.getFuture().get(10, java.util.concurrent.TimeUnit.SECONDS);
            if (!confirm.isAck() || correlation.getReturned() != null) {
                throw new IllegalStateException("Order event was not accepted and routed by RabbitMQ");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted awaiting RabbitMQ confirmation", e);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            throw new IllegalStateException("RabbitMQ did not confirm the order event", e);
        }
    }
}
