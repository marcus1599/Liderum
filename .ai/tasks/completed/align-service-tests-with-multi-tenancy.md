# Tarefa Técnica — Alinhar Testes de Serviços com Multi-tenancy

> **Classificação:** SMALL
>
> **Status:** Concluída localmente; ainda não commitada

## Contexto

Quatro testes de serviço foram adaptados para o uso já existente de `TenantService` e consultas filtradas por `Guild`: Attendance, Event, Member e Team.

Essas alterações são independentes da correção do segredo JWT. Foram encontradas no working tree durante a validação e não devem ser justificadas como parte da task de segurança JWT.

## Escopo

- Validar os ajustes em `AttendanceServiceImplTest`, `EventServiceImplTest`, `MemberServiceImplTest` e `TeamServiceImplTest`.
- Confirmar que os mocks e as expectativas refletem o isolamento multi-tenant implementado nos serviços e repositórios.
- Executar `./mvnw clean verify` no backend.

## Fora do escopo

- Alterar a implementação dos serviços ou repositórios.
- Alterar regras de autenticação/JWT.
- Criar migrations ou modificar o frontend.

## Critérios de conclusão

- Os quatro testes passam na suíte backend.
- As alterações permanecem limitadas à adaptação dos testes ao multi-tenancy existente.
- A task JWT permanece separada e validada localmente.

## Resultado verificado (2026-08-16)

`./mvnw.cmd clean verify` foi executado com sucesso: 24 testes, 0 failures e 0 errors. Os quatro testes desta task passaram.

## Pendência não bloqueante

O Maven reporta dependência duplicada `org.junit.jupiter:junit-jupiter` no `pom.xml` (linha 133). A correção está fora do escopo desta task e deve ser tratada separadamente.
