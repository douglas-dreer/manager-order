package io.github.douglasdreer.managerorder.application.mapper;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.domain.entity.Order;
import io.github.douglasdreer.managerorder.domain.entity.OrderItem;

/**
 * Mapeador responsável pela conversão entre DTOs e entidades de pedidos.
 *
 * Realiza a transformação bidirecional entre os objetos de transferência de dados (DTOs)
 * e as entidades de domínio (Order e OrderItem), garantindo a correta vinculação
 * entre entidades pai e filho, bem como o cálculo de valores totais.
 *
 * @see OrderInputDTO
 * @see OrderOutputDTO
 * @see Order
 * @see OrderItem
 */
public interface OrderMapper {
    /**
     * Converte um DTO de entrada em uma entidade Order.
     *
     * Realiza o mapeamento dos dados de entrada para a entidade de domínio,
     * criando um novo pedido com status RECEIVED e vinculando todos os itens
     * fornecidos. A relação bidirecional entre Order e OrderItem é estabelecida
     * durante este processo.
     *
     * @param dto DTO contendo os dados de entrada do pedido.
     *            Deve conter externalId válido e lista de itens não vazia.
     * @return Uma nova instância de Order com status RECEIVED,
     *         externalId do DTO e itens vinculados.
     *
     * @example
     * OrderInputDTO dto = new OrderInputDTO(
     *     "EXT-2024-001",
     *     List.of(
     *         new OrderItemInputDTO("Notebook", new BigDecimal("2500.00"), 2)
     *     )
     * );
     * Order order = mapper.toEntity(dto);
     * // order.getStatus() == OrderStatus.RECEIVED
     * // order.getItems().size() == 1
     *
     * @see OrderInputDTO
     * @see Order
     */
    Order toEntity(OrderInputDTO dto);
    /**
     * Converte uma entidade Order em um DTO de saída.
     *
     * Realiza o mapeamento da entidade de domínio para o DTO de resposta,
     * transformando todos os itens associados e preservando os valores calculados
     * como totalValue de cada item e do pedido completo.
     *
     * @param entity Entidade Order a ser convertida.
     *               Deve conter id, externalId, status, createdAt e items populados.
     * @return DTO contendo todas as informações do pedido e seus itens
     *         com valores totais calculados.
     *
     * @example
     * Order order = // entidade Order populada
     * OrderOutputDTO dto = mapper.toOutput(order);
     * // dto.getId() == order.getId()
     * // dto.getItems().get(0).getTotalValue() == order.getItems().get(0).getTotalValue()
     *
     * @see OrderOutputDTO
     * @see Order
     */
    OrderOutputDTO toDto(Order entity);
}

