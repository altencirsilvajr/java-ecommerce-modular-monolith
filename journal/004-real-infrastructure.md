# 004 - Infraestrutura real em testes

## Commit

`test: validate real infrastructure boundaries`

## Objetivo

Provar que migrations, cache e broker funcionam contra PostgreSQL, Redis e RabbitMQ reais.

## Implementacao

- Testcontainers inicia as tres dependencias com configuracao dinamica.
- Testes verificam os seis schemas e conexoes reais ao cache e broker.

## Rastreabilidade ADR

ADR aplicado: ADR-0002 - Checkout eventualmente consistente com Outbox e Inbox.

## Verificacao

- `./mvnw test -Dtest=InfrastructureTest`: 2 testes aprovados com PostgreSQL 17, Redis 8 e RabbitMQ 3.13 reais.
- A primeira execucao revelou que Spring Boot 4 requer `spring-boot-starter-flyway`; a segunda revelou permissao ausente no vhost do broker. Ambas foram corrigidas antes do resultado verde.

## Alternativas e trade-offs

O teste usa Docker e pode ser ignorado automaticamente quando o daemon nao esta disponivel.

## Proximo passo

Entregar UI e automacao operacional.
