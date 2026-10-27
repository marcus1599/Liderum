# Plan — `decouple-team-leadership-from-user-status`

## Classification and routing

MEDIUM / structural schema and Team-flow change; security-sensitive tenant-scoped reassignment, but no auth policy changes.

```text
Routing:
- Domains: architecture, backend, database, frontend, testing, security, audit, documentation
- Agents: Architect consultative review → Backend Dev → Frontend Dev → QA → Security → Auditor
- Skills: create-migration, test-backend, test-frontend, ux-review, Impeccable clarify/audit, security-review, audit-task, finish-task
- SRE not selected: no infrastructure or deployment changes
- RabbitMQ and roadmap execution not selected: unrelated and the UX roadmap is already COMPLETED
- Gate: migration and server-side membership invariant → Angular behavior → targeted/full tests and builds → QA/Security/Auditor; finish-task only after Task Verdict APROVADO
```

## Implementation sequence

1. Keep this focused task active; inspect migrations, current contracts, Group UI, membership moves/removals, and User offboarding behavior.
2. Review the Member-based leader relation, data backfill, legacy FK, and concurrency strategy against current relational constraints.
3. Add `V5` as an expand-contract migration: nullable `team.leader_member_id`, a same-Team composite FK and index, and backfill only when the legacy leader User points to a Member already in that Team and Guild. Drop the old User FK but retain the old column and values unchanged.
4. Update Team entity/repositories/service/controller/DTOs to expose `leaderMemberId`/`leaderNickname` and accept Member identity. Candidate data is scoped to the selected Team; validate membership and Guild on every mutation.
5. Use one lock order for membership/leader mutations: lock Member first, then affected Team rows in ascending ID order. Apply it to reassignment, Team add/remove/delete, and Member create/update/delete. Clear the source Team leader before moving/removing/deleting that Member, within the same transaction, and flush before membership changes to satisfy the composite FK.
6. New Teams start without a leader. Remove User-status/deletion coupling to the tactical Team marker while preserving unrelated account-lifecycle protections.
7. Update Angular service, Groups component/template/specs to select current group Members by Member id and nickname; preserve manager-only controls, server-confirmed updates, keyboard access, and safe feedback.
8. Add migration/backfill, backend service/HTTP/RBAC/tenant/lifecycle/concurrency tests and frontend contract/empty/error/success tests.
9. Run targeted backend tests, full `mvnw.cmd clean verify`, Angular tests and build, diff checks, Impeccable detector, and available rendered inspection. Record exact evidence and limitations.
10. Run formal QA, Security, and Audit gates. Only finish/archive after approval; no push. Commit is separate and requires explicit authorization if `.git` writes are blocked.

## Key files

- Backend: `Team`, `User`, `TeamServiceImpl`, `MemberServiceImpl`, `TeamRepository`, `MemberRepository`, `UserServiceImpl`, Team DTOs/controller, Flyway V5, team/member/offboarding integration and service tests.
- Frontend: `group.service.ts`, `groups.component.ts/html/spec.ts`, group service specs.
- Governance: this task's execution/gates, `.ai/state.md`, `.ai/handoff.md`; preserve the completed roadmap checkpoint.

## Risks / completion criteria

- Tenant isolation is proven by a Team in another Guild and a same-Guild Member not assigned to the requested Team being rejected without mutation.
- Existing Team manager roles remain the only authorized mutation roles.
- Membership move/removal and leader update are atomic and leave no team leader pointing at a non-member.
- Migration preserves legacy leader IDs in an inert column, removes only their obsolete FK, and backfills only matching same-Team/same-Guild associations; H2/Flyway migration behavior is tested. Production counts and PostgreSQL execution are reported only if directly observed.
- Build/tests and formal gates pass; visual inspection is reported only if actually rendered.
