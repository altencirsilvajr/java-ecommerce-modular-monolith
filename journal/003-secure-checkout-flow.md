# 003 - Checkout seguro e confiavel

## Commit

`feat: deliver secure asynchronous checkout`

## Objetivo

Permitir que administradores cadastrem produto/estoque e clientes executem checkout idempotente, observando confirmacao ou cancelamento.

## Implementacao

- JWT, RBAC e ownership; Catalog com cache Redis; estoque com lock e idempotencia.
- Orders e Payments em schemas proprios, eventos de dominio/integracao, Outbox/Inbox e publicacao RabbitMQ.
- Problem Details, correlation ID, health, metricas e OpenAPI.

## Rastreabilidade ADR

Novo ADR criado: ADR-0002 - Checkout eventualmente consistente com Outbox e Inbox.

## Verificacao

- RED: `./mvnw test -Dtest=CheckoutFlowTest` executou 5 testes; todos falharam no seam de login com HTTP 403 antes da implementacao.
- GREEN: `./mvnw test -Dtest=CheckoutFlowTest -q` aprovou 5 cenarios REST; `./mvnw test -Dtest=ArchitectureTest -q` aprovou 2 verificacoes modulares.

## Alternativas e trade-offs

Pagamento fake rejeita deterministicamente totais a partir de 5000 para tornar a falha demonstravel e repetivel.

## Proximo passo

Adicionar testes com infraestrutura real, UI Angular e artefatos de entrega.
