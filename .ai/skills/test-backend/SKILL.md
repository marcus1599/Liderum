---
name: test-backend
description: Valide mudanças Java/Spring Boot do Liderum com Maven Wrapper, comportamento afetado e independência de ambiente local.
---
# Test Backend
**Consumidores:** Backend Dev, QA. Leia plano/PRD, diff, testes e `backend/pom.xml`.
## Procedimento
Defina cenários feliz/erro/borda/Guild; execute testes afetados e `./mvnw clean verify` ou `./mvnw.cmd clean verify` quando necessário. Use configuração explícita de teste, nunca secret local implícito; registre comandos, quantidade, failures/errors.
## Bloqueadores
Não altere testes apenas para passar. Falha pré-existente fora de escopo deve ser registrada e requer autorização para correção. Teste dependente de ambiente local reprova.
## Saída
Cenários, comandos, resultados e lacunas.
