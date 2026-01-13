package io.github.douglasdreer.managerorder.application.dto;

import io.github.douglasdreer.managerorder.domain.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * DTO para representar os dados de saída de um pedido.
 *
 * Encapsula as informações completas de um pedido após seu processamento,
 * incluindo identificadores, status, valores calculados e lista de itens.
 * Utilizado como resposta nas operações de consulta e criação de pedidos.
 *
 * @param orderId Identificador único interno do pedido no sistema.
 *                Atribuído automaticamente pelo banco de dados.
 * @param externalId Identificador único externo do pedido.
 *                   Utilizado para rastreamento e integração com sistemas externos.
 * @param totalValue Valor total do pedido em BigDecimal.
 *                   Calculado como a soma de todos os totalAmount dos itens.
 *                   Sempre um valor positivo ou zero.
 * @param status Status atual do pedido.
 *               Define o estado do processamento do pedido.
 * @param createdAt Data e hora de criação do pedido.
 *                  Registrada automaticamente no momento da persistência.
 * @param items Lista de itens que compõem o pedido.
 *              Contém todos os produtos incluídos no pedido com seus detalhes.
 *
 * @example
 * new OrderOutputDTO(
 *     1L,
 *     "EXT-2024-001",
 *     new BigDecimal("5150.00"),
 *     OrderStatus.PENDING,
 *     LocalDateTime.now(),
 *     List.of(
 *         new OrderItemOutputDTO("Notebook Dell", new BigDecimal("2500.00"), 2, new BigDecimal("5000.00")),
 *         new OrderItemOutputDTO("Mouse Logitech", new BigDecimal("150.00"), 1, new BigDecimal("150.00"))
 *     )
 * )
 *
 * @see OrderItemOutputDTO
 * @see OrderStatus
 * @see io.github.douglasdreer.managerorder.application.dto
 */
public record OrderOutputDTO(
        Long orderId,
        String externalId,
        BigDecimal totalValue,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemOutputDTO> items
) {
}
