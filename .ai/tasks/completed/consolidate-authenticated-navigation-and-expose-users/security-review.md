# Security Review — consolidate-authenticated-navigation-and-expose-users

## Superfície revisada

- Visibilidade condicional do link Users;
- guard e agrupamento das rotas autenticadas;
- origem do perfil usado na apresentação;
- fluxo de logout e navegação.

## Evidências

- `/dashboard`, `/users`, `/members`, `/teams`, `/events` e `/attendance` estão sob a rota de `AuthenticatedLayoutComponent` com `authGuard`.
- O menu lê somente `AuthService.profile$`, alimentado pelo perfil atual de `/users/me`. Quando o perfil é `null`, o link Users não aparece.
- Apenas MARECHAL/GENERAL veem o link. A tela Users mantém a verificação atual e não chama a listagem para perfil não administrativo; o backend conserva suas próprias anotações de autorização.
- Não foram adicionados `guildId`, role de JWT, dados do cliente ou novo endpoint como autoridade; nenhum código de autenticação, autorização ou tenant foi modificado.
- Logout continua encaminhado do sidebar para `AuthService.logout()` no layout compartilhado.

## Achados

Nenhuma vulnerabilidade introduzida ou agravada pela task. A ocultação do link é usabilidade, não uma barreira de segurança; acesso direto continua submetido ao guard, às verificações da tela e à autorização backend.

## Vereditos

- **Task Security Verdict: APROVADO**
- **Release Verdict: APROVADO** — esta task não adiciona bloqueador global; estado global anterior estava aprovado.
