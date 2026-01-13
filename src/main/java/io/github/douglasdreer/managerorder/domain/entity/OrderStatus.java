package io.github.douglasdreer.managerorder.domain.entity;

/**
 * Representa os estados do ciclo de vida de um pedido no sistema.
 *
 * <p>Este enum define os pontos de controle do fluxo de processamento,
 * desde o recebimento inicial até a conclusão ou falha.</p>
 *
 * <p>Os estados podem ser usados para rastreamento, auditoria e
 * controle de integração entre serviços.</p>
 *
 * @since 1.0
 */
public enum OrderStatus {
    /**
     * Pedido recebido da fila de mensagens, ainda não processado.
     * Estado inicial do fluxo.
     */
    RECEIVED,
    /**
     * Valores e regras de negócio do pedido foram calculados com sucesso.
     * Indica que o pedido está pronto para envio ao serviço de produtos.
     */
    CALCULATED,
    /**
     * Pedido enviado com sucesso para o Produto B.
     * Estado terminal de sucesso.
     */
    PROCESSED,
    /**
     * Ocorreu uma falha em qualquer etapa do processamento.
     * Estado terminal de erro.
     */
    ERROR
}