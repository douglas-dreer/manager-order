package io.github.douglasdreer.managerorder.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Nomes das filas e exchanges
    public static final String IMPORT_QUEUE = "q.orders.import";
    public static final String IMPORT_DLQ = "q.orders.import.dlq";
    public static final String MAIN_EXCHANGE = "ex.orders.main";
    public static final String DLX_EXCHANGE = "ex.orders.dlx";

    // Routing Keys
    public static final String RK_IMPORT = "order.imported";
    public static final String RK_ERROR = "order.error";

    @Bean
    public Queue importQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", RK_ERROR);
        return new Queue(IMPORT_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue importDlq() {
        return new Queue(IMPORT_DLQ, true);
    }

    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(MAIN_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Binding mainBinding(Queue importQueue, TopicExchange mainExchange) {
        return BindingBuilder.bind(importQueue).to(mainExchange).with(RK_IMPORT);
    }

    @Bean
    public Binding dlqBinding(Queue importDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(importDlq).to(deadLetterExchange).with(RK_ERROR);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new JacksonJsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}