# Inventário — desativação segura de Staff

Inspeção estática em 2026-10-17. Nenhum código de produto foi alterado neste planejamento.

## Prioridade

`product-evolution-roadmap.md` aponta desativação segura de Staff como próxima capacidade do Ciclo 3 após a alteração autenticada de senha. `UserStatus.DISABLED` existe, porém sem workflow administrativo. O usuário decidiu adiar o erro RabbitMQ 406 por enquanto; ele permanece risco conhecido de release e não é dependência funcional desta task.

## Código observado

- `User.status` admite `PENDING`, `ACTIVE`, `DISABLED`. Criação administrativa produz `PENDING`; ativação produz `ACTIVE`; `CustomUserDetails.isEnabled()` e `JwtFilter` recusam conta não `ACTIVE`.
- JWT tem `uid`/`cv` verificados contra o User persistido. Troca de senha incrementa `credentialVersion`; desativação deve usar a mesma barreira para que JWT anterior não volte a funcionar após reativação.
- `UserController` oferece criação, regeneração, leitura, role, exclusão, perfil e senha própria, mas não mudança de status. Frontend `/users` exibe status, sem ações de desativar/reativar.
- `UserServiceImpl` resolve ator/alvo via `TenantService`; RBAC MARECHAL/GENERAL existe. A proteção atual conta todos os MARECHAIS, inclusive PENDING/DISABLED, não apenas ACTIVE. Há lock pessimista na consulta dos MARECHAIS.
- `Team.leader` é FK opcional para User. `TeamServiceImpl.update` altera somente nome; embora `TeamRequestDTO` tenha `leaderId`, não há reassociação de líder implementada.
- `UserActivationToken` atende contas PENDING; não deve ser usado como fluxo de reativação.
- Há múltiplos exception handlers com mensagens diferentes; novo conflito precisa de resposta HTTP segura, verificada por teste.

## Recomendação

Implementar transições `ACTIVE → DISABLED → ACTIVE`, preservando User, senha hash e relações. Invalidar os JWTs com incremento de `credentialVersion`. Recusar auto-desativação e preservar ao menos um MARECHAL ACTIVE em transições de status, mudança de role e exclusão.

Para User que lidera Team, recomendação é bloquear desativação com conflito seguro e sem mutação, até que a reassociação de líder exista em task própria. Não trocar a liderança automaticamente nem incorporar a UX de Team nesta task. Esta decisão aguarda resposta do usuário.
