# SDD ativo — E-commerce Modular Monolith

## Resultado

Entregar um laboratorio executavel em Java 25, Spring Boot 4.1 e Angular 22.1 com cinco modulos protegidos: Users, Catalog, Inventory, Orders e Payments.

## Seams e criterios

- REST/OpenAPI: autenticacao, catalogo, estoque e pedidos retornam Problem Details em falhas.
- Checkout: o comando idempotente retorna `202`; consultas revelam transicoes assincronas ate confirmacao ou cancelamento.
- Eventos: Outbox e Inbox persistentes tornam publicacao e consumo repetivel seguros.
- Arquitetura: Spring Modulith e ArchUnit detectam dependencias ilegais.
- Operacao: PostgreSQL, Redis e RabbitMQ sobem via Compose; health, metricas, logs correlacionados e tracing ficam expostos.
- UI: Angular consome a API real para login, catalogo e checkout.

## Fora do escopo

Carrinho persistente, frete, cupons, provedor de pagamento real, marketplace e cluster Kubernetes em execucao.
