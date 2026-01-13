package io.github.douglasdreer.managerorder.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para representar um item de pedido.
 *
 * Contém as informações necessárias para criar ou atualizar um item
 * dentro de um pedido, incluindo validações de negócio.
 *
 * @param productName Nome do produto. Não pode ser vazio ou nulo.
 * @param unitPrice Preço unitário do produto em BigDecimal.
 *                  Deve ser um valor positivo e maior que zero.
 * @param quantity Quantidade de unidades do produto no pedido.
 *                 Deve ser um inteiro positivo e maior que zero.
 *
 * @example
 * new OrderItemInputDTO(
 *     "Notebook Dell",
 *     new BigDecimal("2500.00"),
 *     2
 * )
 *
 * @see io.github.douglasdreer.managerorder.application.dto
 */
public record OrderItemInputDTO(
        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal unitPrice,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity
) {}
