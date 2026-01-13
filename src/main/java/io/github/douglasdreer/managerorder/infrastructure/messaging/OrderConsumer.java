package io.github.douglasdreer.managerorder.infrastructure.messaging;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.domain.service.OrderService;
import io.github.douglasdreer.managerorder.infrastructure.config.RabbitMQConfig;
import io.github.douglasdreer.managerorder.infrastructure.messaging.impl.OrderProducerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderService orderService;
    private final OrderProducerImpl orderProducer;

    /**
     * Ouve a fila de importação. Se um erro fatal ocorrer,
     * a configuração do RabbitMQ cuidará do roteamento para a DLQ.
     */
    @RabbitListener(queues = RabbitMQConfig.IMPORT_QUEUE)
    public void consumeOrder(OrderInputDTO input) {
        log.info("Iniciando percepção do pedido: {}", input.externalId());

        try {
            // A transformação acontece aqui
            OrderOutputDTO processedOrder = orderService.processOrder(input);

            // Manifesta o resultado para o próximo serviço (Produto B)
            orderProducer.sendCalculatedOrder(processedOrder);

            log.info("Pedido {} processado e enviado com sucesso.", input.externalId());

        } catch (Exception e) {
            log.error("Falha na conexão com os dados do pedido {}: {}", input.externalId(), e.getMessage());
            // Ao lançar a exceção, o RabbitMQ entende que deve enviar para a DLQ configurada no Bean
            throw e;
        }
    }
}