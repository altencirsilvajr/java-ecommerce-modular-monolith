# 005 - UI e entrega operacional

## Commit

`feat: add Angular learning UI and delivery assets`

## Objetivo

Tornar o laboratorio demonstravel pelo navegador e reproduzivel em pipelines e ambientes containerizados.

## Implementacao

- Angular 22.1 para login, catalogo, checkout e polling do pedido.
- Imagens sem root, Compose, Kubernetes/OpenShift, GitHub Actions, Jenkins e GitLab CI.
- README PT-BR com arquitetura, execucao, verificacao e roteiro de entrevista.

## Rastreabilidade ADR

ADR aplicado: ADR-0001 - Monolito modular com Spring Modulith.

## Verificacao

- `npm --prefix frontend run test:ci`: 1 teste aprovado.
- `npm --prefix frontend run build`: bundle de producao gerado (210.25 kB inicial).
- `docker compose config --quiet`: aprovado.
- `docker build -t java-ecommerce-modular-monolith:test .`: imagem backend criada.
- `docker build -t java-ecommerce-ui:test frontend`: imagem frontend criada.
- `npm audit --json`: 0 vulnerabilidades high/critical e 3 moderate em tooling Angular; nao ha versao 22.1 corrigida disponivel em 2026-08-05.
- `ruby` parseou os manifests Kubernetes/OpenShift; `kubectl --dry-run=client` nao foi usado como evidencia porque tentou consultar um cluster local inexistente.
- `FRONTEND_PORT=14200 docker compose up -d --wait`: cinco servicos saudaveis; readiness retornou `UP` e a UI retornou o titulo esperado. A porta 14200 evitou conflito local em 4200.
- Gate final: `./mvnw verify` aprovou 9 testes; `npm ci`, teste Angular, build Angular e `docker compose config --quiet` foram aprovados.

## Alternativas e trade-offs

A UI e um painel de aprendizagem, nao um storefront; regras de negocio permanecem no backend.

## Proximo passo

Executar o gate completo e registrar evidencias finais.
