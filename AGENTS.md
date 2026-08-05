# Repository instructions

- Keep production code, identifiers and commits in English; write documentation in PT-BR.
- Work on `main` in atomic vertical slices; the public history is part of the portfolio evidence.
- Every substantive non-merge commit must contain exactly one changed file under `journal/`.
- Record durable decisions under `docs/adr/` and keep `docs/sdd/active.md` truthful.
- Keep Users, Catalog, Inventory, Orders and Payments as Spring Modulith application modules; communicate through public contracts and events.
- Never commit credentials or claim verification that was not observed.

## Required verification

```bash
./scripts/verify-traceability.sh --staged
git diff --check --cached
JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./mvnw verify
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend ci
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend run test:ci
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend run build
docker compose config --quiet
```
