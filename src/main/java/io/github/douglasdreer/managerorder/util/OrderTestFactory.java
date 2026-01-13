package io.github.douglasdreer.managerorder.util;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderItemInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderItemOutputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.domain.entity.Order;
import io.github.douglasdreer.managerorder.domain.entity.OrderItem;
import io.github.douglasdreer.managerorder.domain.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Factory utilitária para criação de objetos de domínio e DTOs
 * utilizados exclusivamente em testes.
 * <p>
 * Centraliza a criação de entidades e objetos de entrada,
 * reduzindo duplicação de código e garantindo consistência
 * entre os cenários de teste.
 * <p>
 * Esta classe não contém regras de negócio e não deve ser
 * utilizada em código de produção.
 *
 * @author Douglas Dreer
 * @since 1.0
 */
public final class OrderTestFactory {

    private static final String DEFAULT_PRODUCT_NAME = "product-1";
    private static final BigDecimal DEFAULT_UNIT_PRICE = BigDecimal.valueOf(50);
    private static final int DEFAULT_QUANTITY = 2;

    private OrderTestFactory() {}

    /**
     * Cria um pedido básico válido, sem itens associados.
     *
     * @return pedido em estado inicial
     */
    public static Order createOrder() {
        return Order.builder()
                .externalId(generateExternalId())
                .build();
    }

    /**
     * Cria um pedido com um ID específico.
     * @param id ID do pedido
     * @return {@link Order} com ID definido
     */
    public static Order createOrderWithId(Long id) {
        Order order = createOrder();
        order.setId(id);
        return order;
    }

    /**
     * Cria um pedido com dois itens associados.
     *
     * @return pedido com itens
     */
    public static Order createOrderWithItems() {
        Order order = createOrder();
        List<OrderItem> items = Collections.singletonList(createItem(order));
        order.setItems(items);
        return order;
    }

    /**
     * Cria um pedido com itens e valor total calculado.
     *
     * @return pedido com status CALCULATED
     */
    public static Order createCalculatedOrder() {
        Order order = createOrderWithItems();
        order.setStatus(OrderStatus.CALCULATED);

        order.setTotalValue(calculateTotalValue(order));
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    /**
     * Cria um pedido já processado.
     *
     * @return pedido com status PROCESSED
     */
    public static Order createProcessedOrder() {
        Order order = createOrder();
        order.setStatus(OrderStatus.PROCESSED);
        return order;
    }

    /**
     * Cria e associa um item a um pedido existente,
     * mantendo a consistência da relação bidirecional.
     *
     * @param order pedido ao qual o item será associado
     * @return item criado
     */
    public static OrderItem createItem(Order order) {
        OrderItem item = OrderItem.builder()
                .productName(DEFAULT_PRODUCT_NAME)
                .unitPrice(DEFAULT_UNIT_PRICE)
                .quantity(DEFAULT_QUANTITY)
                .build();

        order.addItem(item);
        return item;
    }

    /**
     * Cria um {@link OrderInputDTO} válido para testes de API
     * e camada de aplicação.
     *
     * @return DTO de entrada de pedido
     */
    public static OrderInputDTO createOrderInputDTO() {
        return new OrderInputDTO(
                generateExternalId(),
                List.of(createItemInputDTO())
        );
    }



    /**
     * Cria um item de pedido para uso em {@link OrderInputDTO}.
     *
     * @return item de entrada válido
     */
    private static OrderItemInputDTO createItemInputDTO() {
        return new OrderItemInputDTO(
                DEFAULT_PRODUCT_NAME,
                DEFAULT_UNIT_PRICE,
                DEFAULT_QUANTITY
        );
    }

    /**
     * Cria um item de pedido para uso em {@link OrderOutputDTO}.
     * @return item de saída válido
     */
    private static OrderItemOutputDTO createItemOutputDTO() {
        return new OrderItemOutputDTO(
                DEFAULT_PRODUCT_NAME,
                DEFAULT_UNIT_PRICE,
                DEFAULT_QUANTITY,
                DEFAULT_UNIT_PRICE.multiply(BigDecimal.valueOf(DEFAULT_QUANTITY))
        );
    }

    /**
     * Gera um identificador externo único para evitar
     * conflitos de chave em testes de integração.
     *
     * @return externalId único
     */
    private static String generateExternalId() {
        return "external-" + UUID.randomUUID();
    }

    /**
     * Calcula o valor total de um pedido com base nos seus itens.
     * @return {@link BigDecimal} representando o valor total do pedido
     */
    private static BigDecimal calculateTotalValue(Order order) {
        return order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Cria um {@link OrderOutputDTO} válido para testes de API
     * e camada de aplicação.
     * @return DTO de saída de pedido
     */
    public static OrderOutputDTO createOrderOutputDTO() {
        return new OrderOutputDTO(
                1L,
                generateExternalId(),
                DEFAULT_UNIT_PRICE,
                OrderStatus.CALCULATED,
                LocalDateTime.now(),
                List.of(createItemOutputDTO())
        );
    }
}
