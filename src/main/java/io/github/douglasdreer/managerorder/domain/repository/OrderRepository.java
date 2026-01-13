package io.github.douglasdreer.managerorder.domain.repository;

import io.github.douglasdreer.managerorder.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório de domínio responsável pelo acesso e persistência
 * de {@link Order}.
 *
 * <p>Define operações de consulta específicas do domínio,
 * abstraindo detalhes de persistência e infraestrutura.</p>
 *
 * @since 1.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Recupera um pedido a partir do identificador externo.
     *
     * <p>O {@code externalId} é originado em um sistema externo
     * e possui restrição de unicidade, garantindo no máximo
     * um resultado.</p>
     *
     * @param externalId identificador externo do pedido
     * @return {@link Optional} contendo o pedido, caso exista
     */
    Optional<Order> findByExternalId(String externalId);
}
