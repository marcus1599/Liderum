# PRD — Decouple team leadership from user-account status

## Objective

Represent a Team leader as a Member assigned to that same Team. Leadership is a tactical, changeable organization label for GvG; it is independent of User account existence/status and Guild strategic role.

## Confirmed business rules

- Any Member currently assigned to a Team is eligible to lead that Team, regardless of GuildRole, linked User, or UserStatus.
- A leader must be a Member of the same Team. Members may be moved between Teams, and leadership may change at any time.
- Removing or moving the current leader out of a Team clears that Team's leader assignment. It must not silently leave an outsider marked as leader.
- Newly created Teams have no leader until a Member is added and selected.
- Existing RBAC for changing Team membership/leadership remains unchanged: MARECHAL, GENERAL, and MAJOR. Tenant authority remains server-side.
- Disabling or deleting a linked User does not alter a Member's Team membership or tactical leadership marker.

## Scope

- Change Team's persisted leader association from User to Member and add an additive Flyway migration.
- Backfill the new association only where the legacy User is linked to a Member already assigned to that same Team and Guild; leave all other new leader associations null.
- Scope leadership candidates to the selected Team's existing Member list; expose only Member id and nickname needed by the selector.
- Validate membership and same-Guild ownership on every reassignment.
- Clear leadership when its Member is removed or moved to another Team, whether the operation comes through Team management or Member create/update/delete.
- Update Team request/response contracts and Angular Groups UI/specs to use Member identifiers and labels.
- Remove the User-status deactivation check that treated Team leadership as an account lifecycle dependency.
- Update historical task/state references only where needed to describe the superseding rule; do not rewrite prior execution evidence.

## Out of scope

- Changing roles allowed to manage Teams or members.
- Changing account authentication, activation, deactivation credentials, or tenant resolution.
- Adding a Member-to-User association or requiring Members to have accounts.
- Changing GvG behavior outside the leadership marker.
- Altering the completed UX roadmap or RabbitMQ status.
- Dropping the legacy `team.leader_id` column or its stored values in this task. Keep that column and its historical values inert; remove only its obsolete FK so User lifecycle is no longer coupled to Team leadership.

## Acceptance criteria

1. A same-Team Member without a linked User can be selected as leader.
2. A Member linked to a PENDING or DISABLED User can be selected; account status is not part of leader eligibility.
3. Members of another Team, another Guild, or with no Team association cannot be selected or assigned by direct HTTP request.
4. Existing role authorization and server-side tenant isolation remain enforced.
5. Creating a Team leaves its leader null; after adding Members, one can be selected.
6. Removing/moving a leader Member clears the source Team leader atomically with membership change.
7. User account deactivation/deletion is not blocked solely because its linked Member is a Team leader and does not clear that marker.
8. Migration applies on supported test databases and backfills only same-Team/same-Guild linked Members; unmatched legacy leaders become null in the new field without destroying the legacy field.
9. UI clearly states eligibility (current group members), shows Member nickname, and handles no-member/no-alternative, loading, success, and errors accessibly.
10. Backend and Angular tests, builds, security review, QA, and audit pass; limitations are recorded without claiming unobserved evidence.

## Risks and migration

- `team.leader_id` currently references `users.id`, while the new relation references `member.id`. Use an expand-contract migration `V5`: add indexed nullable `leader_member_id`, a same-Team composite FK, and backfill only a valid same-Team/same-Guild User→Member association. Drop obsolete `fk_team_leader` but preserve the old column and values inert. Report matched/unmatched counts only from test fixtures; do not infer production counts.
- Team leader becomes an optional tactical annotation, so clearing it on Member move/removal/deletion is expected and must be transactional. Use a shared lock order (Member first, affected Teams by ascending id) for operations that change membership or leadership.
- This changes Team JSON identity to `leaderMemberId`/`leaderNickname`, and the reassignment request to `leaderMemberId`. Repository search found the in-repository Angular consumer; update it and its tests. Do not accept a client-supplied Guild or User id as authority.
- The global Release Verdict remains independently blocked by the deferred RabbitMQ validation; this task does not resume it.
