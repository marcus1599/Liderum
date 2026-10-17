# Inventário — alteração autenticada de senha

Inspeção estática em 2026-10-13, base `c74e528`. Git inicialmente limpo e `main` sincronizada com `origin/main`; usuário confirmou o push. Não foram executadas suítes, requests de produção ou alterações de produto neste planejamento.

## Prioridade

| Candidata | Evidência e decisão |
| --- | --- |
| Alteração autenticada de senha | Primeiro item NEXT do roadmap; contas individuais já existem, mas não há endpoint nem tela para o titular trocar a credencial. Recomendada como próxima task P1 de funcionalidade/segurança. |
| Desativação segura de Staff | Continua necessária; requer política própria de último MARECHAL ACTIVE, vínculos de liderança e reativação. Não será absorvida pela troca de senha. |
| Escolha/alteração de líder de Team | Candidata UX do Ciclo 3: `TeamRequestDTO` aceita `leaderId` na criação; `TeamServiceImpl.update` altera somente nome. Exige decisão funcional própria, não apenas seletor visual. Adiada em favor do controle de credencial individual. |
| Erros backend e RBAC visual de domínio | Encaminhamentos da task de feedback HTTP; análise dedicada permanece no backlog, sem assumir exploit de produção a partir de inspeção estática. |

As seis fases originais e os Ciclos 1/2 permanecem concluídos. O Ciclo 3 permanece em andamento; esta task concretiza a capacidade NEXT de identidade individual, sem declarar concluída a escolha de líder.

## Código observado

- `Controllers/UserController.java`: perfil próprio em `GET /users/me`; CRUD administrativo, role e regeneração de ativação. Não existe alteração autenticada de senha.
- `Services/Impl/UserServiceImpl.java`: usa `TenantService` para o ator e a Guild; não altera senha de conta ACTIVE. `Entities/User.java` tem password/status/Guild; não tem versão de credencial.
- `Security/JwtUtil.java`: JWT com subject username, roles, issued-at e expiration; valida assinatura/expiração. `application.properties` define expiração padrão de 604800000 ms (sete dias), configurável por ambiente. Nenhuma comparação com estado da credencial.
- `Controllers/AuthController.java`: autentica a senha e emite JWT. `JwtFilter` carrega UserDetails persistido, usa suas authorities, mas não confere `isEnabled()` antes de autenticar. `UserDetailsServiceImpl` retorna o usuário sem recusar status; `CustomUserDetails.isEnabled()` só retorna true para ACTIVE. O caminho de login e o caminho Bearer não aplicam a mesma verificação de status.
- `Tenancy/TenantService.java`: recarrega User pelo nome autenticado e resolve Guild persistida; continuará sendo a fonte de autoridade tenant.
- `Config/SecurityConfig.java`: BCrypt, sessão stateless e endpoints protegidos por padrão; não define AuthenticationEntryPoint explícito. O status efetivo de autenticação inválida deverá ser comprovado em HTTP, sem supor 401 pelo frontend.
- `dto/ActivationRequestDTO.java` e `GuildRegistrationRequestDTO.java`: validam 8–128 caracteres. O limite de bytes do BCrypt não está representado nesses DTOs; o novo fluxo terá limite explícito, sem refatorar esses contratos nesta task.
- `exceptions/ApiExceptionHandler.java`, `GlobalExceptionHandler.java` e `Handlers/ValidationExceptionHandler.java`: múltiplos advices; alguns retornam mensagens de exceção. O novo contrato de erro precisa ser seguro e testado através do handler efetivo, sem consolidar globalmente os advices nesta task.
- `frontend/.../auth/AuthService`: token em localStorage, perfil em memória, restauração por `/users/me`, logout remove token/perfil. Interceptor expira sessão em 401 de chamada protegida.
- `app.routes.ts` e `shared/sidebar`: shell autenticado já compartilhado. Não há área de senha; link Users é restrito a MARECHAL/GENERAL e não serve como entrada para todos os titulares.
- Reactive Forms, Angular Material, campos password/confirmation, feedback e tema escuro já existem em login/registro/ativação. Reutilizar padrões sem modificar esses fluxos.
- Flyway aplicado até V3; H2 e testes PostgreSQL fresh/upgrade existentes. Próxima versão esperada: V4, reconfirmar antes de implementar.

## Achados de segurança do planejamento

| Origem | Evidência | Implicação |
| --- | --- | --- |
| PREEXISTENTE_RELACIONADO | Confirmado no código: JWT não é vinculado à versão da credencial | Trocar só BCrypt não revoga sessões; precisa ser resolvido para o objetivo desta task. |
| PREEXISTENTE_RELACIONADO | Confirmado no código: filtro Bearer não verifica isEnabled | A nova operação deve recusar PENDING/DISABLED; incluir checagem ACTIVE no boundary de autenticação e teste de JWT real. Não há endpoint público de desativação implementado; não afirmar exploração de produção. |
| PREEXISTENTE_RELACIONADO | Potencial: advices podem expor detalhes de exceções | Novos erros e falhas de parsing precisam de resposta local segura; nenhuma senha ou valor rejeitado em corpo/log. Refactor global continua separado. |
| PREEXISTENTE_NAO_RELACIONADO | Registro/ativação usam limite por caracteres; RBAC visual incompleto registrado anteriormente | Preservar contratos existentes; avaliar separadamente. A task não certifica toda a política de senhas/dependências do produto. |

Não há implementação nova a aprovar neste documento. Release Verdict conhecido: APROVADO pela última auditoria, não revalidado neste planejamento. Segurança e impacto de deploy desta proposta exigem gates próprios.

## Referências técnicas

- [OWASP Authentication — Change Password Feature](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html#change-password-feature): sessão válida e verificação da senha atual.
- [OWASP Password Storage — bcrypt](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#bcrypt): limite de 72 bytes ao manter BCrypt.
- [Spring Security — Password Storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html): usar PasswordEncoder para hash/comparação; confirmar comportamento da versão resolvida pelo projeto nos testes.
