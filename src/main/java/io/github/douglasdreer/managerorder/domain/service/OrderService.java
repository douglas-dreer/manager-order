package io.github.douglasdreer.managerorder.domain.service;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;

/**
 * Interface que define o contrato para operações de processamento de pedidos.
 *
 * Responsável por orquestrar a lógica de negócio relacionada a pedidos,
 * incluindo validação de duplicidade, cálculo de valores totais e persistência
 * dos dados. Abstrai a implementação específica do serviço, permitindo múltiplas
 * estratégias de processamento conforme necessário.
 *
 * @see OrderInputDTO
 * @see OrderOutputDTO
 */
public interface OrderService {
    /**
     * Processa um novo pedido completo no sistema.
     *
     * Realiza as seguintes operações em sequência:
     * 1. Valida se o pedido (identificado pelo externalId) já existe no sistema
     * 2. Converte o DTO de entrada em uma entidade Order
     * 3. Calcula os valores totais de cada item e do pedido completo
     * 4. Persiste o pedido no banco de dados
     * 5. Converte a entidade persistida em um DTO de saída
     *
     * @param input DTO contendo os dados de entrada do pedido.
     *              Deve conter externalId válido e lista de itens não vazia.
     * @return DTO de saída com todas as informações do pedido processado,
     *         incluindo identificador atribuído, status RECEIVED e valores totais calculados.
     *
     * @example
     * OrderInputDTO input = new OrderInputDTO(
     *     "EXT-2024-001",
     *     List.of(
     *         new OrderItemInputDTO("Notebook Dell", new BigDecimal("2500.00"), 2),
     *         new OrderItemInputDTO("Mouse Logitech", new BigDecimal("150.00"), 1)
     *     )
     * );
     * OrderOutputDTO output = orderService.processOrder(input);
     * // output.orderId() == 1L (gerado pelo banco)
     * // output.status() == OrderStatus.RECEIVED
     * // output.totalValue() == BigDecimal("5150.00")
     *
     * @throws IllegalArgumentException se o externalId já existe no sistema
     *
     * @see OrderInputDTO
     * @see OrderOutputDTO
     */
    OrderOutputDTO processOrder(OrderInputDTO input);
}