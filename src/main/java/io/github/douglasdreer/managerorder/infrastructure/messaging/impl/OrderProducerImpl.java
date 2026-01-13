package io.github.douglasdreer.managerorder.infrastructure.messaging.impl;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.domain.entity.OrderStatus;
import io.github.douglasdreer.managerorder.infrastructure.config.RabbitMQConfig;
import io.github.douglasdreer.managerorder.infrastructure.messaging.OrderProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducerImpl implements OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    @CircuitBreaker(name = "orderService", fallbackMethod = "processOrderFallback")
    public void sendCalculatedOrder(OrderOutputDTO order) {
        log.info("Enviando pedido calculado {} para a fila do Produto B", order.externalId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MAIN_EXCHANGE,
                RabbitMQConfig.RK_IMPORT,
                order
        );
    }

    public OrderOutputDTO processOrderFallback(OrderInputDTO input, Throwable ex) {
        log.error("Circuit breaker ativo ou erro ao processar o pedido {}: {}", input.externalId(), ex.getMessage());
        return new OrderOutputDTO(null, input.externalId(), null, OrderStatus.ERROR, null, null);
    }
}