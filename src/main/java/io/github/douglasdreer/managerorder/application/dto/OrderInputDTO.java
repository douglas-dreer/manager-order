package io.github.douglasdreer.managerorder.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO para representar os dados de entrada de um pedido.
 *
 * Encapsula as informações necessárias para criar ou atualizar um pedido,
 * incluindo o identificador externo e a lista de itens que compõem o pedido.
 * Todas as validações são aplicadas em cascata através da anotação @Valid.
 *
 * @param externalId Identificador único externo do pedido.
 *                   Não pode ser vazio ou nulo.
 *                   Utilizado para rastreamento e integração com sistemas externos.
 * @param items Lista de itens que compõem o pedido.
 *              Deve conter pelo menos um item.
 *              Não pode ser nula ou vazia.
 *              Cada item é validado individualmente através da anotação @Valid.
 *
 * @example
 * new OrderInputDTO(
 *     "EXT-2024-001",
 *     List.of(
 *         new OrderItemInputDTO("Notebook Dell", new BigDecimal("2500.00"), 2),
 *         new OrderItemInputDTO("Mouse Logitech", new BigDecimal("150.00"), 1)
 *     )
 * )
 *
 * @see OrderItemInputDTO
 * @see io.github.douglasdreer.managerorder.application.dto
 */
public record OrderInputDTO(
        @NotBlank(message = "External ID is required")
        String externalId,

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        List<OrderItemInputDTO> items
) {
}
