package io.github.douglasdreer.managerorder.domain.service.impl;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.application.mapper.OrderMapper;
import io.github.douglasdreer.managerorder.domain.entity.Order;
import io.github.douglasdreer.managerorder.domain.repository.OrderRepository;
import io.github.douglasdreer.managerorder.domain.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementação padrão do serviço de processamento de pedidos.
 *
 * Orquestra a lógica de negócio para criação e processamento de pedidos,
 * incluindo validação de duplicidade, cálculo de valores e persistência.
 * Utiliza transações para garantir a integridade dos dados e logging
 * para rastreabilidade das operações.
 *
 * @see OrderService
 * @see OrderMapper
 * @see OrderRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderOutputDTO processOrder(OrderInputDTO input) {
        log.info("Processing order. ExternalId={}", input.externalId());

        return findExistingOrder(input.externalId())
                .orElseGet(() -> createAndPersistOrder(input));
    }

    /**
     * Busca um pedido existente no repositório pelo identificador externo.
     *
     * Se encontrado, registra um aviso no log e retorna o DTO de saída
     * do pedido existente. Caso contrário, retorna um Optional vazio.
     *
     * @param externalId Identificador externo do pedido a ser buscado.
     * @return Optional contendo o DTO de saída do pedido se encontrado,
     *         ou Optional vazio se não existir.
     *
     * @see OrderRepository#findByExternalId(String)
     */
    private Optional<OrderOutputDTO> findExistingOrder(String externalId) {
        return orderRepository.findByExternalId(externalId)
                .map(order -> {
                    log.warn("Pedido já existe. ExternalId={}", externalId);
                    return orderMapper.toDto(order);
                });
    }

    /**
     * Cria e persiste um novo pedido no sistema com tratamento de race condition.
     *
     * Realiza as seguintes operações:
     * <ul>
     *     <ol>1. Converte o DTO de entrada em uma entidade Order</ol>
     *     <ol>2. Calcula os valores totais do pedido e seus itens</ol>
     *     <ol>3. Persiste o pedido no banco de dados usando saveAndFlush</ol>
     *     <ol>4. Registra o sucesso no log</ol>
     *     <ol>5. Converte a entidade persistida em DTO de saída</ol>
     * </ul>
     *
     * Em caso de `DataIntegrityViolationException`, trata a race condition
     * recuperando o pedido existente que foi criado simultaneamente.
     *
     * @param input DTO contendo os dados de entrada do pedido.
     * @return DTO de saída com o pedido processado e persistido,
     *         incluindo identificador gerado pelo banco de dados.
     * @throws IllegalStateException se o pedido for detectado como duplicado
     *         mas não puder ser recuperado do banco de dados.
     *
     * @see OrderMapper#toEntity(OrderInputDTO)
     * @see Order#calculateTotal()
     * @see OrderRepository#saveAndFlush(Object)
     */
    private OrderOutputDTO createAndPersistOrder(OrderInputDTO input) {
        Order order = orderMapper.toEntity(input);
        order.calculateTotal();

        try {
            Order savedOrder = orderRepository.saveAndFlush(order);
            log.info("O pedido foi criado com sucesso. id={}", savedOrder.getId());
            return orderMapper.toDto(savedOrder);

        } catch (DataIntegrityViolationException ex) {
            log.warn(
                    "A criação simultânea de pedidos foi detectada. Recuperando o pedido existente. ExternalId={}",
                    input.externalId(),
                    ex
            );

            return orderRepository.findByExternalId(input.externalId())
                    .map(orderMapper::toDto)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "O pedido existe, mas não pôde ser recuperado. ExternalId=" + input.externalId(),
                                    ex
                            )
                    );
        }
    }
}
