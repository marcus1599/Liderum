# SRE/DevOps — establish-fullstack-ci-and-compose-demo-environment

## Veredito

**APROVADO**

## Evidências operacionais

- Compose sobe PostgreSQL 18.6, RabbitMQ, backend, notification-consumer e frontend com exit code 0.
- O volume local nomeado usa `/var/lib/postgresql`, o layout compatível com PostgreSQL 18+.
- PostgreSQL respondeu `pg_isready`; RabbitMQ respondeu ao diagnóstico de ping; frontend e Management UI responderam HTTP 200.
- CI adicional cobre testes/build Angular e testes/package do notification-service sem dependência de segredos externos.

## Riscos conhecidos

- O primeiro download de dependências pode ser demorado; `.dockerignore` reduz os contextos de build.
- A imagem Node reporta vulnerabilidades transitivas existentes; remediação de dependências não pertence ao escopo desta task.
