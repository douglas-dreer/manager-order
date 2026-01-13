package io.github.douglasdreer.managerorder.domain.service.impl;

import io.github.douglasdreer.managerorder.AbstractIntegrationTest;
import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderItemOutputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.domain.entity.Order;
import io.github.douglasdreer.managerorder.domain.entity.OrderStatus;
import io.github.douglasdreer.managerorder.domain.repository.OrderRepository;
import io.github.douglasdreer.managerorder.domain.service.OrderService;
import io.github.douglasdreer.managerorder.util.OrderTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE) // Não precisamos de porta Web aberta aqui
class OrderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // Como o container é compartilhado (static no AbstractIntegrationTest),
        // limpamos o banco antes de cada teste para garantir isolamento.
        orderRepository.deleteAll();
    }

    /**
     * Testa o fluxo completo de processamento de um pedido:
     * cálculo do total, persistência no banco e retorno do DTO.
     */
    @Test
    @DisplayName("Integração: Deve calcular total, salvar no Postgres e retornar DTO")
    void shouldProcessAndPersistOrder() {
        // Arrange
        OrderInputDTO inputDTO = OrderTestFactory.createOrderInputDTO();
        BigDecimal expectedTotal = calculateExpectedTotal(inputDTO);

        // Act
        OrderOutputDTO result = orderService.processOrder(inputDTO);

        // Assert (Retorno do Service)
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isNotNull();
        assertThat(result.status()).isEqualTo(OrderStatus.CALCULATED);

        assertThat(result.totalValue()).isEqualByComparingTo(expectedTotal);

        // Assert (Efeito Colateral no Banco de Dados Real)
        Optional<Order> savedOrder = orderRepository.findById(result.orderId());
        assertThat(savedOrder).isPresent();
        assertThat(savedOrder.get().getExternalId()).isEqualTo(inputDTO.externalId());
        assertThat(savedOrder.get().getTotalValue()).isEqualByComparingTo(expectedTotal);
    }

    /**
     * Testa a idempotência do processamento de pedidos.
     * Chama o serviço duas vezes com o mesmo DTO de entrada
     * e verifica que o pedido não é duplicado no banco.
     */
    @Test
    @DisplayName("Integração: Deve garantir Idempotência (não duplicar no banco)")
    void shouldNotDuplicateInDatabase() {
        // Arrange
        OrderInputDTO inputDTO = OrderTestFactory.createOrderInputDTO();

        // Act 1: Primeira chamada (Salva)
        OrderOutputDTO result1 = orderService.processOrder(inputDTO);

        // Act 2: Segunda chamada (Simula reenvio da fila)
        OrderOutputDTO result2 = orderService.processOrder(inputDTO);
        BigDecimal valueAfterSecondCall = calculateExpectedTotal(inputDTO);

        // Assert
        assertThat(result1.orderId()).isEqualTo(result2.orderId()); // Devem ter o mesmo ID de banco

        // Verifica se realmente só existe 1 registro no banco
        long count = orderRepository.count();
        assertThat(count).isEqualTo(1);

        // Verifica se o objeto retornado na segunda vez ainda está correto
        assertThat(result2.totalValue()).isEqualByComparingTo(valueAfterSecondCall);
    }

    /**
     * Calcula o valor total esperado do pedido com base nos itens do DTO de entrada.
     * @param inputDTO
     * @return valor total esperado
     */
    private BigDecimal calculateExpectedTotal(OrderInputDTO inputDTO) {
        return inputDTO.items().stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}