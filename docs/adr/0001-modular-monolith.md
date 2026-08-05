# ADR-0001 - Monolito modular com Spring Modulith

## Status

Aceito

## Contexto

O laboratorio precisa demonstrar limites de dominio fortes sem assumir o custo operacional de cinco servicos. Checkout exige consistencia local e colaboracao assincrona entre capacidades.

## Decisao

Users, Catalog, Inventory, Orders e Payments sao modulos de aplicacao Spring Modulith. Dependencias sao verificadas em build; dados pertencem a schemas PostgreSQL distintos; comunicacao transversal usa contratos publicos ou eventos.

## Consequencias

- Um unico deploy e transacoes locais simples.
- Limites precisam de testes automatizados para evitar erosao.
- Extracao futura permanece possivel, mas nao e promessa de arquitetura.

## Alternativas rejeitadas

### Microservices

Custos de rede, deploy e observabilidade nao agregariam aprendizado proporcional ao fluxo compacto.
