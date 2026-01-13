package io.github.douglasdreer.managerorder.application.dto;

import java.math.BigDecimal;

/**
 * DTO para representar um item de pedido na resposta da aplicação.
 *
 * Contém as informações finais de um item após o processamento do pedido,
 * incluindo o valor total calculado do item.
 *
 * @param productName Nome do produto. Identifica o produto no pedido.
 * @param unitPrice Preço unitário do produto em BigDecimal.
 *                  Representa o valor cobrado por unidade.
 * @param quantity Quantidade de unidades do produto no pedido.
 *                 Representa o número de unidades adquiridas.
 * @param totalAmount Valor total do item em BigDecimal.
 *                    Calculado como: unitPrice × quantity.
 *                    Sempre um valor positivo.
 *
 * @example
 * new OrderItemOutputDTO(
 *     "Notebook Dell",
 *     new BigDecimal("2500.00"),
 *     2,
 *     new BigDecimal("5000.00")
 * )
 *
 * @see OrderOutputDTO
 * @see io.github.douglasdreer.managerorder.application.dto
 */
public record OrderItemOutputDTO(
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalAmount
) {
}

