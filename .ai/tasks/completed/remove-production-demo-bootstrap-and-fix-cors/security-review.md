# Security Review — remove-production-demo-bootstrap-and-fix-cors

## Task Security Verdict

**APROVADO**

## Release Verdict

**BLOQUEADO**

## Checklist e evidências

| Área | Evidência | Resultado |
| --- | --- | --- |
| Bootstrap demo | `@Profile("dev")`; default e `prod` validados sem seeds | mitigado |
| Credencial demo | permanece confinada ao código de seed dev; não é criada fora de dev e não foi adicionada a config/documentação de produção | mitigado |
| Origins | `CORS_ALLOWED_ORIGINS` é particionado em valores individuais | mitigado |
| Wildcard/credenciais | não há `allowedOrigins("*")` ou `allowedOriginPatterns`; credenciais só são aceitas para origins explícitas | mitigado |
| Preflight | `CorsConfigurationSource` é integrado ao Spring Security; origin não permitida/lista vazia recebe 403 em teste | mitigado |
| JWT/RBAC/onboarding | sem mudança de contrato; regressões de onboarding, tenancy e RBAC passaram | preservado |
| Secrets | `.env.example` contém somente placeholder JWT e URL local não secreta | aprovado |

## Achados e origem

| Achado | Origem | Evidência | Encaminhamento |
| --- | --- | --- | --- |
| Bootstrap de Guild/MARECHAL demo fora de dev | PREEXISTENTE_RELACIONADO | corrigido e coberto por testes | resolvido nesta task |
| CORS com origins em string única | PREEXISTENTE_RELACIONADO | corrigido e coberto por MockMvc | resolvido nesta task |
| Registro público sem rate limiting/CAPTCHA | PREEXISTENTE_NAO_RELACIONADO | permanece fora do diff | mantém bloqueador de release/task própria |
| Migrations Flyway ausentes | PREEXISTENTE_NAO_RELACIONADO | avisos de validação sem migrations | mantém bloqueador de release/task própria |

Nenhuma vulnerabilidade introduzida ou agravada foi identificada.

## SRE/DevOps

**APROVADO.** Docker Compose mantém `SPRING_PROFILES_ACTIVE=dev` explicitamente, apropriado apenas para demonstração local. A origem CORS é configurável por `CORS_ALLOWED_ORIGINS`; produção sem essa variável permanece restritiva. Não foram adicionados serviços, segredos ou dependências.
