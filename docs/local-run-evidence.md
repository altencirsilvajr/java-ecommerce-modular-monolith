# Evidencia de execucao local

Validacao realizada em 2026-08-05 com Docker Desktop 29.6.2.

```text
FRONTEND_PORT=14200 docker compose up -d --wait --wait-timeout 120
postgres   healthy
redis      healthy
rabbitmq   healthy
api        healthy
frontend   healthy

GET http://localhost:8080/actuator/health/readiness
{"status":"UP"}

GET http://localhost:14200/
<title>E-commerce Lab
```

A primeira tentativa encontrou a porta 4200 ocupada por outro laboratorio; a repeticao usou 14200, conforme o override documentado no Compose. Ao final, `docker compose down -v` removeu containers, rede e volume criados nesta validacao.
