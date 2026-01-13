package io.github.douglasdreer.managerorder.domain.service.impl;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.application.mapper.OrderMapper;
import io.github.douglasdreer.managerorder.domain.entity.Order;
import io.github.douglasdreer.managerorder.domain.repository.OrderRepository;
import io.github.douglasdreer.managerorder.util.OrderTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    /**
     * Verifica o comportamento do serviço ao criar um novo pedido
     * quando o externalId não existe no sistema.
     *
     * Deve mapear, persistir e retornar o pedido criado.
     */
    @Test
    @DisplayName("Should create and persist a new order successfully")
    void shouldCreateNewOrderSuccessfully() {
        // Arrange
        OrderInputDTO inputDTO = OrderTestFactory.createOrderInputDTO();
        Order entity = OrderTestFactory.createOrder();
        OrderOutputDTO expectedOutput = OrderTestFactory.createOrderOutputDTO();

        when(orderRepository.findByExternalId(inputDTO.externalId()))
                .thenReturn(Optional.empty());

        when(orderMapper.toEntity(inputDTO))
                .thenReturn(entity);

        when(orderRepository.saveAndFlush(entity))
                .thenReturn(entity);

        when(orderMapper.toDto(entity))
                .thenReturn(expectedOutput);

        // Act
        OrderOutputDTO result = orderService.processOrder(inputDTO);

        // Assert
        assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedOutput);

        // Verify
        verify(orderRepository).findByExternalId(inputDTO.externalId());
        verify(orderMapper).toEntity(inputDTO);
        verify(orderRepository).saveAndFlush(entity);
        verify(orderMapper).toDto(entity);
    }

    /**
     * Verifica o comportamento do serviço ao receber um pedido
     * com um externalId que já existe no sistema.
     *
     * Deve retornar o pedido existente sem criar um novo.
     */
    @Test
    @DisplayName("Should return existing order when externalId already exists (idempotency)")
    void shouldReturnExistingOrderWhenDuplicated() {
        // Arrange
        OrderInputDTO input = OrderTestFactory.createOrderInputDTO();
        Order existingOrder = OrderTestFactory.createCalculatedOrder();
        OrderOutputDTO expectedOutput = OrderTestFactory.createOrderOutputDTO();

        when(orderRepository.findByExternalId(input.externalId())).thenReturn(Optional.of(existingOrder));

        when(orderMapper.toDto(existingOrder))
                .thenReturn(expectedOutput);

        // Act
        OrderOutputDTO result = orderService.processOrder(input);

        // Assert
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedOutput);

        verify(orderRepository).findByExternalId(input.externalId());

        verify(orderMapper).toDto(existingOrder);

        verify(orderRepository, never()).save(any());
        verify(orderRepository, never()).saveAndFlush(any());
        verify(orderMapper, never()).toEntity(any());

        verifyNoMoreInteractions(orderRepository, orderMapper);
    }

    /**
     * Trata cenários de concorrência onde múltiplos pedidos com o mesmo externalId
     * podem ser criados simultaneamente.
     *
     * Em caso de violação de unicidade, tenta recuperar o pedido persistido
     * pela transação concorrente.
     */
    @Test
    @DisplayName("Should recover order when concurrent creation causes unique constraint violation")
    void shouldRecoverOrderWhenUniqueConstraintViolationOccurs() {
        // Arrange
        OrderInputDTO input = OrderTestFactory.createOrderInputDTO();
        Order orderEntity = OrderTestFactory.createOrder();
        Order existingOrder = OrderTestFactory.createCalculatedOrder();
        OrderOutputDTO expectedOutput = OrderTestFactory.createOrderOutputDTO();

        when(orderRepository.findByExternalId(input.externalId()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(input))
                .thenReturn(orderEntity);

        when(orderRepository.saveAndFlush(any(Order.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate external_id"));

        when(orderMapper.toDto(existingOrder))
                .thenReturn(expectedOutput);

        // Act
        OrderOutputDTO result = orderService.processOrder(input);

        // Assert
        assertThat(result).isEqualTo(expectedOutput);

        verify(orderRepository, times(2)).findByExternalId(input.externalId());
        verify(orderRepository).saveAndFlush(any(Order.class));
        verify(orderMapper).toDto(existingOrder);
    }

    /**
     * Trata cenários de concorrência onde múltiplos pedidos com o mesmo externalId
     * podem ser criados simultaneamente.
     *
     * Em caso de violação de unicidade, tenta recuperar o pedido persistido
     * pela transação concorrente.
     *
     * @throws IllegalStateException se o pedido existir no banco, mas não puder ser recuperado
     */
    @Test
    @DisplayName("Should throw IllegalStateException when unique constraint violation occurs and order cannot be recovered")
    void shouldThrowIllegalStateExceptionWhenOrderCannotBeRecoveredAfterConstraintViolation() {
        // Arrange
        OrderInputDTO input = OrderTestFactory.createOrderInputDTO();
        Order orderEntity = OrderTestFactory.createOrder();

        when(orderRepository.findByExternalId(input.externalId()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        when(orderMapper.toEntity(input))
                .thenReturn(orderEntity);

        when(orderRepository.saveAndFlush(any(Order.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate external_id"));

        // Act + Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.processOrder(input)
        );

        // Assert — mensagem
        assertThat(exception.getMessage())
                .contains("O pedido existe, mas não pôde ser recuperado")
                .contains(input.externalId());

        // Assert — causa
        assertThat(exception.getCause())
                .isInstanceOf(DataIntegrityViolationException.class);

        // Verify — fluxo completo
        verify(orderRepository, times(2))
                .findByExternalId(input.externalId());

        verify(orderRepository)
                .saveAndFlush(any(Order.class));

        verify(orderMapper)
                .toEntity(input);

        verifyNoMoreInteractions(orderRepository, orderMapper);
    }

}
