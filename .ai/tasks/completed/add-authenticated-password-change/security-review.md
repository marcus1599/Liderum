# Security Review — add-authenticated-password-change

## Task Security Verdict

**APROVADO.** Código, testes H2 e migrations PostgreSQL 18 fresh/upgrade revisados; nenhum risco bloqueante introduzido que impeça a task de ser auditada.

## Release Verdict

**BLOQUEADO para promover esta implementação** até que o rollout assegure a substituição/drain de instâncias antigas que ainda aceitam JWTs sem `uid`/`cv`. Nenhum deploy foi executado. O estado global de produção além desta alteração não foi revalidado.

## Checklist baseado em evidência

- Alvo é `TenantService.getCurrentUser()` / principal autenticado; o endpoint não recebe identificador de alvo. `uid`/`cv` apenas confirmam identidade/versão no JWT e não são autoridade de Guild/role.
- `JwtFilter` exige `CustomUserDetails`, ACTIVE, subject/uid/cv correspondentes ao User persistido; authorities derivam do User carregado. H2 Integration cobre JWT antigo, versão divergente, DISABLED e usuário apagado/recriado.
- `PasswordChangeServiceImpl` lê senha persistida com BCrypt, altera só o próprio hash e versão em transação sob `PESSIMISTIC_WRITE`; refresh posterior ao lock evita validar snapshot obsoleto. O teste concorrente passou na suíte.
- Migration é aditiva, default zero e NOT NULL; nenhum dado de senha, status, Guild, role ou hash é reescrito. PostgreSQL 18.6 descartável aplicou V1–V4 fresh e V4 sobre V3 com Users existentes; ACTIVE/PENDING, hash, Guild e status foram preservados, Hibernate `validate` e contexto Spring passaram.
- Request rejeita campos desconhecidos; testes provam que `userId`, `guildId`, role e credentialVersion enviados não alteram outra conta/Guild.
- DTO não contém `toString` gerado e nenhuma resposta inclui hash, senha ou credentialVersion. Mensagens do novo caminho são fixas/códigos allowlisted; teste HTTP verificou código seguro e ausência de segredo no DTO de perfil.
- A UI valida limite BCrypt em UTF-8, mantém senhas somente em memória e limpa campos/sessão após sucesso ou saída; resultados ambíguos não repetem automaticamente a mutation.
- Nenhuma senha/token/Authorization é registrada por código da task; nenhum secret real ou dependência foi adicionado.

## Achados e operação

1. **INFORMATIVO — comportamento de sessão aprovado:** no primeiro deploy, JWTs legados sem `uid`/`cv` serão recusados; usuários precisarão autenticar novamente. Isto consta do PRD e foi aceito junto ao escopo. Não altera dados nem senha.
2. **RISCO OPERACIONAL — ainda requer SRE:** enquanto instâncias antigas estiverem servindo tráfego durante rollout/rollback, seu filtro antigo ignora `uid`/`cv` e pode continuar aceitando tokens antigos. A implantação deve garantir a substituição efetiva antes de considerar revogação global concluída; rollback para código antigo reabilitaria essa aceitação. Nenhum deploy foi executado.
3. **INFORMATIVO / DÍVIDA TÉCNICA:** Flyway 9.22.3 avisa que PostgreSQL 18 está acima da versão 15 declarada como testada. PostgreSQL 18.6 executou com sucesso os testes desta task; avaliar upgrade do Flyway em task separada, sem mudança automática nesta task.

4. **LIMITAÇÃO DE EVIDÊNCIA UX:** sem sessão manual autenticada em desktop/handset; revisão estática e testes DOM não equivalem a inspeção visual completa. Não afeta a segurança da barreira de autenticação nem o contrato backend.

Achados preexistentes não relacionados não foram reauditorados nesta revisão. ADR-001 permaneceu intacto; ADR-002 recomendado não foi criado sem autorização explícita. Nenhum banco de produção foi acessado.
