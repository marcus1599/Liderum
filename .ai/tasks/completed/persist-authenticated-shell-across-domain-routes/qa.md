# QA — Persistência do Shell Autenticado

## Veredito

APROVADO

## Evidências

- A navegação de domínio foi movida para um layout autenticado com `router-outlet` filho.
- `/users`, `/members`, `/teams`, `/events` e `/attendance` permanecem sob `authGuard`.
- Login, registro e ativação permanecem fora do shell.
- O Sidebar continua fechando o drawer somente em handset.
- Suíte Angular: 35/35 SUCCESS.
- Build Angular: SUCCESS, exit code 0.

Os warnings de Sass, budget e `MatTable` não utilizado são preexistentes/não bloqueantes e não alteram o veredito.
