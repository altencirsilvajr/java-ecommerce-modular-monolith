# 002 - Fundacao modular

## Commit

`feat: establish modular application foundation`

## Objetivo

Disponibilizar uma aplicacao Java 25/Spring Boot 4.1 cujo build protege os cinco limites de negocio.

## Implementacao

- Maven Wrapper e dependencias de runtime/testes.
- Spring Modulith com Users, Catalog, Inventory, Orders e Payments.
- Testes publicos de estrutura modular.

## Rastreabilidade ADR

Novo ADR criado: ADR-0001 - Monolito modular com Spring Modulith.

## Verificacao

- O teste estrutural foi escrito como contrato antes da verificacao da fundacao.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./mvnw test -Dtest=ArchitectureTest`: 2 testes aprovados.

## Alternativas e trade-offs

Spring Modulith torna os limites executaveis sem distribuir o sistema.

## Proximo passo

Implementar autenticacao, catalogo e estoque pelo seam REST.
