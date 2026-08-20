# Execução — Remover JWT/Authorization header dos logs do JwtFilter

## Implementação

- Removidos os `System.out.println` de `JwtFilter` que expunham header Authorization, JWT, resultado de validação, username, roles e estado de autenticação.
- Não foram alterados protocolo JWT, claims, segredo, expiração, endpoints, regras de autorização, dependências ou schema.

## Teste adicionado

`JwtFilterTest` usa JWT/usuário/role sintéticos e um `OutputCaptureExtension` para confirmar simultaneamente que:

- um token válido ainda cria autenticação com `ROLE_MARECHAL`;
- o filter chain continua sendo chamado;
- header Authorization, token, username e role não aparecem na saída padrão.

## Resultados

```text
.\mvnw.cmd -Dtest=JwtFilterTest test
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

.\mvnw.cmd clean verify
Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
```

## QA

**APROVADO.** A validação cobre o comportamento preservado e a ausência de exposição, sem estado externo ou segredo da máquina.
