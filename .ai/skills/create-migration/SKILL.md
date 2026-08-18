---
name: create-migration
description: Planeje ou crie migration Flyway para mudança estrutural do banco Liderum; nunca edite migration aplicada.
---
# Create Migration
**Consumidores:** Backend Dev, Arquiteto consultivamente. Leia plano/PRD, `lib.md`, migrations, entidades e repositórios.
## Procedimento
Inspecione versões, determine próxima migration em `backend/src/main/resources/db/migration/`, use `V<versao>__descricao.sql`, avalie PostgreSQL/H2, dados, FKs e índices. Para rename/drop, proponha expand-contract e peça confirmação. Valide aplicação/migration e registre resultado.
## Bloqueadores
Schema sem migration, migration aplicada editada, FK sem avaliação de índice ou operação destrutiva sem plano reprovam. DOWN migration não é exigida.
## Saída
Versão, arquivo, impacto, mitigação e validação.
