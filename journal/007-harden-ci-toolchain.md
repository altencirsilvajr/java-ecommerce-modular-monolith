# 007 - Endurecer toolchain de CI

## Commit

`ci: eliminate toolchain warnings`

## Objetivo

Remover alertas do frontend e o aviso de runtime legado do checkout.

## Implementacao

- Fixa a cadeia transitiva corrigida de `@hono/node-server` em 2.1.0.
- Registra allowlist versionada para scripts de instalacao do Angular.
- Atualiza checkout para Node 24 e promove `npm audit` a gate.

## Rastreabilidade ADR

Decisao local sem ADR novo: manutencao reversivel sem alterar os modulos de negocio.

## Verificacao

- `npm audit`: 0 vulnerabilidades e nenhum script pendente.
- Teste frontend: 1 aprovado; build Angular aprovado.
- Workflow validado como YAML e sem Actions antigas.
