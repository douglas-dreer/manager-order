package io.github.douglasdreer.managerorder.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de domínio que representa um pedido no sistema.
 *
 * <p>O {@code Order} é o agregado raiz responsável por manter a
 * consistência dos {@link OrderItem} associados, bem como controlar
 * o estado e o valor total do pedido ao longo do seu ciclo de vida.</p>
 *
 * <p>Pedidos são identificados externamente por um {@code externalId},
 * garantindo idempotência na integração com sistemas produtores.</p>
 *
 * @since 1.0
 */
@Entity
@Table(
        name = "tb_orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_external_order_id",
                        columnNames = "external_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    /**
     * Identificador interno do pedido.
     *
     * <p>Gerado automaticamente pelo banco de dados.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador externo do pedido, originado no Produto A.
     *
     * <p>Campo imutável após criação e único no sistema,
     * utilizado para garantir idempotência.</p>
     */
    @Column(name = "external_id", nullable = false, updatable = false)
    private String externalId;

    /**
     * Data e hora de criação do pedido.
     *
     * <p>Definido automaticamente no momento da persistência.</p>
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Estado atual do pedido dentro do seu ciclo de vida.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.RECEIVED;

    /**
     * Valor total do pedido.
     *
     * <p>Calculado a partir da soma do valor total de todos os itens.</p>
     */
    @Column(name = "total_value", precision = 19, scale = 2)
    private BigDecimal totalValue;

    /**
     * Itens associados a este pedido.
     *
     * <p>Relacionamento gerenciado pelo agregado raiz, com
     * propagação de operações e remoção automática de órfãos.</p>
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Versão da entidade para controle de concorrência otimista.
     *
     * <p>Utilizado para evitar sobrescrita de dados em atualizações
     * concorrentes.</p>
     */
    @Version
    private Long version;

    /**
     * Adiciona um item ao pedido mantendo a consistência
     * da associação bidirecional.
     *
     * @param item item a ser associado ao pedido
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Calcula o valor total do pedido com base nos itens associados.
     *
     * <p>Após o cálculo bem-sucedido, o estado do pedido é atualizado
     * para {@link OrderStatus#CALCULATED}.</p>
     */
    public void calculateTotal() {
        this.totalValue = items.stream()
                .map(OrderItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.status = OrderStatus.CALCULATED;
    }
}
