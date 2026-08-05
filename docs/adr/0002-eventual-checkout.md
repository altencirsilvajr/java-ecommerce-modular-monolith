# ADR-0002 - Checkout eventualmente consistente com Outbox e Inbox

## Status

Aceito

## Contexto

Reserva de estoque e pagamento nao devem ampliar uma transacao distribuida. Falhas e repeticoes de entrega sao normais em mensageria.

## Decisao

Cada mudanca de estado grava seu evento na Outbox na mesma transacao local. Os modulos reagem assincronamente apos commit e registram `consumer:eventId` na Inbox. Um dispatcher publica a Outbox no exchange duravel `ecommerce.events` do RabbitMQ com semantica at-least-once. PostgreSQL usa um schema por modulo.

## Consequencias

- O pedido explicita estados intermediarios e pode ser consultado durante processamento.
- Consumidores precisam ser idempotentes e o operador precisa observar backlog da Outbox.
- Nao ha rollback distribuido; eventos de rejeicao cancelam o pedido e liberam reserva.

## Alternativas rejeitadas

### Transacao XA

Acoplar banco e broker em two-phase commit elevaria custo operacional e reduziria disponibilidade.
