# Security Review — Event e Attendance Tenant Integration

## Classificação de achados

| Achado | Origem | Resultado |
| --- | --- | --- |
| Event cross-Guild produzia 500 por `RuntimeException` não mapeada | PREEXISTENTE_RELACIONADO | Corrigido com `EntityNotFoundException`; não houve bypass tenant. |
| IDOR em Event/Attendance | Não confirmado | Cenários cross-Guild retornam 404 e não expõem ou alteram dados. |
| Referência Member/Event cross-Guild | Não confirmado | Criação retorna 404 e não deixa Attendance parcial. |

## Veredito de segurança da task

**APROVADO**

- O tenant continua resolvido exclusivamente no servidor, pelo `TenantService` baseado no usuário autenticado; o cliente não fornece `guildId` como autoridade.
- Os testes exercitam IDs pertencentes a Guild B com JWT de administrador da Guild A. Leitura, update e delete de Event/Attendance são negados por 404 e os dados remotos permanecem inalterados.
- Referências cross-Guild na criação de Attendance são resolvidas por Guild corrente e rejeitadas, sem criação parcial.
- A prova de 403 para SOLDADO separa negação RBAC de não enumeração de recursos cross-Guild.
- Não foram adicionados segredos, logs de JWT/Authorization, mass assignment ou elevação de privilégio.

## Release Verdict

**APROVADO** — não há bloqueador global novo. Esta task é de evidência de segurança e qualidade; a correção de mapeamento de Event reduz uma resposta 500 inconsistente sem alterar a autoridade ou a tenancy.
