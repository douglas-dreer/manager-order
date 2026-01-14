# Arquitetura

## Visao geral

O Manager Order Service e um servico Spring Boot voltado para orquestrar pedidos entre sistemas externos. A aplicacao utiliza:

- **PostgreSQL** para persistencia de pedidos e itens.
- **RabbitMQ** para integracao assincrona com outros servicos.
- **Spring Data JPA** para persistencia e controle transacional.

## Componentes principais

- **Camada de aplicacao**: recebe DTOs de entrada, faz mapeamentos e dispara o processamento.
- **Dominio**: entidades e servicos responsaveis pela regra de calculo e status dos pedidos.
- **Infraestrutura**: configuracoes de mensageria e integracao com RabbitMQ.

## Fluxo de processamento

1. Um pedido e recebido via camada de aplicacao.
2. O servico valida idempotencia pelo `externalId`.
3. O pedido tem seus totais calculados e e persistido.
4. Eventos podem ser publicados para integracao externa.

## Observacoes

- A API HTTP ainda nao esta detalhada; este documento foca na arquitetura e integracao.
