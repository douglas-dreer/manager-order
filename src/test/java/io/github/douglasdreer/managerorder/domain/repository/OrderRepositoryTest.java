package io.github.douglasdreer.managerorder.domain.repository;

import io.github.douglasdreer.managerorder.AbstractIntegrationTest;
import io.github.douglasdreer.managerorder.domain.entity.Order;
import io.github.douglasdreer.managerorder.domain.entity.OrderItem;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static io.github.douglasdreer.managerorder.domain.entity.OrderStatus.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PlatformTransactionManager transactionManager;


    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Integração Real: Salva pedido no Postgres Container")
    void shouldSaveOrderWithItems() {
        Order order = Order.builder()
                .externalId("TEST-CONTAINER-01")
                .status(RECEIVED)
                .build();

        OrderItem item = OrderItem.builder()
                .productName("Heineken")
                .quantity(6)
                .unitPrice(new BigDecimal("5.50"))
                .order(order)
                .build();

        order.addItem(item);

        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();
        // Verifica se o ID gerado pelo Postgres é real existe
        assertThat(savedOrder.getId()).isPositive();
    }

    @Test
    @DisplayName("Integração Real: Constraint de Unique Key do Banco")
    void shouldEnforceUniqueExternalIdInDatabase() {
        Order order1 = Order.builder().externalId("DUPLICADO-DB").build();
        orderRepository.saveAndFlush(order1);

        Order order2 = Order.builder().externalId("DUPLICADO-DB").build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            orderRepository.saveAndFlush(order2);
        });
    }


    @Test
    @DisplayName("Integração Real: Optimistic Locking com PlatformTransactionManager")
    void shouldHandleConcurrencyIdeally() {
        // Setup: cria transação com configurações explícitas
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        // 1. Persiste entidade inicial
        Order order = txTemplate.execute(status -> {
            Order newOrder = Order.builder()
                    .externalId("LOCK-TEST-" + System.currentTimeMillis())
                    .build();
            return orderRepository.save(newOrder);
        });

        // 2. Duas leituras em transações distintas
        Order t1 = txTemplate.execute(status ->
                orderRepository.findById(order.getId()).orElseThrow()
        );

        Order t2 = txTemplate.execute(status ->
                orderRepository.findById(order.getId()).orElseThrow()
        );

        // 3. Primeira atualização - sucesso
        txTemplate.executeWithoutResult(status -> {
            t1.setStatus(PROCESSED);
            orderRepository.save(t1);
        });

        // 4. Segunda atualização - deve falhar
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            txTemplate.executeWithoutResult(status -> {
                t2.setStatus(ERROR);
                orderRepository.save(t2);
            });
        });
    }

}