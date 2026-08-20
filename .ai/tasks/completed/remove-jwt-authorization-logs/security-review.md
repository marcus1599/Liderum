# Security Review — Remover JWT/Authorization header dos logs do JwtFilter

## Vereditos

- **Task Security Verdict: APROVADO**
- **Release Verdict: APROVADO**

## Achado remediado

O achado anterior — header Authorization, JWT e dados derivados em logs de `JwtFilter` — era `PREEXISTENTE_RELACIONADO` a esta task de correção. O diff remove exclusivamente as emissões `System.out.println` responsáveis pela exposição.

## Evidências

- Busca no `JwtFilter` não encontra `System.out.println` nem as mensagens de log anteriormente expostas.
- `JwtFilterTest` exerce token válido, preserva `ROLE_MARECHAL` no `SecurityContext` e comprova que token, header, username e role não constam na saída padrão.
- `clean verify` passou com 31 testes, 0 failures e 0 errors.
- Não houve alteração de segredo JWT, claims, expiração, algoritmo, autenticação, autorização, dependências ou configuração runtime.

## Achados atuais

Nenhum achado bloqueante introduzido, agravado ou remanescente nesta superfície. O bloqueador global de release registrado para logs JWT está resolvido por esta task.
