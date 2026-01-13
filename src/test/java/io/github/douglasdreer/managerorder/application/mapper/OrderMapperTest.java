package io.github.douglasdreer.managerorder.application.mapper;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.domain.entity.Order;
import io.github.douglasdreer.managerorder.util.OrderTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapperImpl();

    @Test
    @DisplayName("Deve converter InputDTO para Entidade corretamente")
    void shouldMapInputDtoToEntity() {
        // Arrange (Usando a Factory - Limpo e Direto)
        OrderInputDTO inputDTO = OrderTestFactory.createOrderInputDTO();

        // Act
        Order result = orderMapper.toEntity(inputDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getExternalId()).isEqualTo(inputDTO.externalId());
        assertThat(result.getItems()).hasSize(1);

        // Verifica dados do primeiro item
        var itemResult = result.getItems().getFirst();
        var itemInput = inputDTO.items().getFirst();

        assertThat(itemResult.getProductName()).isEqualTo(itemInput.productName());
        assertThat(itemResult.getOrder()).isEqualTo(result); // Vínculo garantido
    }

    @Test
    @DisplayName("Deve converter Entidade para OutputDTO calculando subtotais")
    void shouldMapEntityToDTODto() {
        // Arrange (Pega um pedido já montado e calculado da Factory)
        Order order = OrderTestFactory.createCalculatedOrder();


        // Act
        OrderOutputDTO result = orderMapper.toDto(order);

        // Assert
        assertThat(result.externalId()).isEqualTo(order.getExternalId());
        assertThat(result.totalValue()).isEqualTo(order.getTotalValue());

        var itemOutput = result.items().getFirst();

        // Validação de cálculo
        BigDecimal expectedSubTotal = order.getTotalValue();
        assertThat(itemOutput.totalAmount()).isEqualByComparingTo(expectedSubTotal);
    }
}