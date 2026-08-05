# 006 - Publicação do pipeline GitHub Actions

## Commit

`ci: publish GitHub Actions workflow`

## Objetivo

Publicar no branch principal o pipeline GitHub Actions que automatiza os gates do laboratório.

## Implementacao

- Workflow de push e pull request com Java 25, Node 24, Maven, Angular, Compose e build da imagem backend.
- Publicação preparada como commit remoto atômico contendo o workflow e este Journal.
- Safety branch local preserva o commit original que não pôde ser enviado pelo token OAuth sem escopo `workflow`.

## Rastreabilidade ADR

Decisao local sem ADR novo: o pipeline automatiza gates existentes sem alterar a arquitetura do produto.

## Verificacao

- `JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./mvnw verify`: passou com 9 testes.
- `PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend ci`: passou com Node 24.19.0.
- `PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend run test:ci`: passou com 1 teste.
- `PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend run build`: passou; bundle inicial de 210,25 kB.
- `git diff --check` e a auditoria de rastreabilidade passaram.
- `git reset --hard origin/main`: restaurou `main` ao commit publicado após a criação da safety branch.

## Alternativas e trade-offs

O push OAuth foi substituído pela GitHub App somente para o commit do workflow, preservando atualização fast-forward e evitando force push.

## Proximo passo

Confirmar o workflow no remoto e observar a execução de CI.
