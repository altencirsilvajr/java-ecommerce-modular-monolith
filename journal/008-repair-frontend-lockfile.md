# 008 - Reparar lockfile do frontend

## Commit

`fix: synchronize frontend lockfiles`

## Objetivo

Restaurar a instalacao reproduzivel do console Angular.

## Implementacao

- Recupera o lockfile integral e sincroniza a politica de scripts com npm 11.17.

## Rastreabilidade ADR

Decisao local sem ADR novo: correcao de supply chain sem alterar modulos de negocio.

## Verificacao

- Lockfile JSON valido; `npm ci` sem warnings.
- Audit: 0 vulnerabilidades; nenhum script pendente.
