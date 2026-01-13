package io.github.douglasdreer.managerorder.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Entidade de domínio que representa um item de pedido.
 *
 * <p>Cada {@code OrderItem} está associado a um {@link Order} e descreve
 * um produto específico, sua quantidade e valor unitário no contexto
 * de um pedido.</p>
 *
 * <p>Esta entidade concentra regras de domínio relacionadas ao cálculo
 * de valores individuais do pedido.</p>
 *
 * @since 1.0
 */
@Entity
@Table(name = "tb_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    /**
     * Identificador único do item do pedido.
     *
     * <p>Gerado automaticamente pelo banco de dados.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do produto associado a este item.
     *
     * <p>Campo obrigatório.</p>
     */
    @Column(nullable = false)
    private String productName;

    /**
     * Quantidade do produto solicitada no pedido.
     *
     * <p>Deve ser maior que zero.</p>
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Valor unitário do produto.
     *
     * <p>Persistido com duas casas decimais para garantir precisão
     * monetária.</p>
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Pedido ao qual este item pertence.
     *
     * <p>Relacionamento muitos-para-um com carregamento tardio
     * ({@link FetchType#LAZY}).</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Calcula o valor total deste item do pedido.
     *
     * <p>O cálculo é realizado multiplicando o valor unitário
     * pela quantidade informada.</p>
     *
     * <p>Caso o valor unitário ou a quantidade estejam ausentes,
     * retorna {@link BigDecimal#ZERO}.</p>
     *
     * @return valor total do item do pedido
     */
    public BigDecimal getTotalAmount() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
