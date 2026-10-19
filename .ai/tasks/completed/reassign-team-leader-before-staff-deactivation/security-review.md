# Security Review — reassign-team-leader-before-staff-deactivation

**Task Security Verdict: APROVADO**
**Release Verdict: BLOQUEADO**

## Checklist com evidências

- **Autenticação/RBAC:** os dois endpoints novos exigem role MARECHAL, GENERAL ou MAJOR via `@PreAuthorize`; testes verificam CAPITÃO/SOLDADO com 403 e acesso anônimo com 401.
- **Tenant/IDOR:** a Guild não vem do cliente. `TenantService` determina `guildId`, e consultas de Team e candidato são limitadas a `id + guildId`; alvo externo/inexistente retorna 404 seguro. Os testes confirmam ausência de mutação cross-Guild.
- **Exposição:** candidatos são projetados somente para `id` e `username`. Não reutiliza `/users` nem retorna email, senha ou entidade User.
- **Mass assignment:** a operação de reassociação usa `TeamLeaderUpdateRequestDTO` explícito; só `leaderId` é aplicado. Teste envia campos adicionais de guild/role/status e confirma que não mudam o estado persistido.
- **Elegibilidade/concorrência:** candidato é bloqueado se não ACTIVE; query com lock ocorre no tenant, seguida de refresh com lock. Status update e reassociação adquirem locks ordenados de MARECHAL primeiro. Teste concorrente garante que não há User DISABLED mantido como líder depois de ambos os requests.
- **Atomicidade:** a reassociação é transacional; falha de validação não altera Team. Testes validam estado persistido nos casos rejeitados.
- **JWT/ADR:** JWT e ADR-001 não foram alterados; `leaderId` é uma referência de recurso e não fonte de autoridade tenant.
- **Secrets/dependências/schema:** nenhum segredo foi adicionado; sem dependências ou migration novas.

## Achados por origem

| Achado | Evidência/origem | Tratamento |
| --- | --- | --- |
| Nenhum risco bloqueante introduzido ou agravado encontrado | Revisão dos diffs de backend/frontend/testes e suíte final verde | Task Security APROVADO |
| RabbitMQ AMQP 406 na fila de notificações permanece registrado no estado do projeto | PREEXISTENTE_NAO_RELACIONADO; fora dos arquivos e do fluxo desta task, previamente adiado pelo usuário | Mantém Release Verdict BLOQUEADO; não reprova esta task |

Nenhuma alteração corretiva de segurança fora do escopo foi feita.
