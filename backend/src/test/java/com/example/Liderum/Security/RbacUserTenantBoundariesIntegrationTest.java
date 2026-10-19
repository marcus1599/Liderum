package com.example.Liderum.Security;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Enums.UserStatus;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Repository.UserActivationTokenRepository;
import com.example.Liderum.Repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test_only_rbac_tenant_boundaries_secret",
        "liderum.registration.rate-limit.limit=100"
})
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class RbacUserTenantBoundariesIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired UserActivationTokenRepository activationTokenRepository;

    @Test
    void generalCanListReadAndCreateOnlyRolesBelowGeneral() throws Exception {
        Actor owner = onboard("general-create-owner");
        Actor general = createActor(owner.token(), "general-create", GuildRole.GENERAL);

        mockMvc.perform(get("/users").header("Authorization", bearer(general.token())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/" + owner.id()).header("Authorization", bearer(general.token())))
                .andExpect(status().isOk());

        for (GuildRole role : new GuildRole[] { GuildRole.MAJOR, GuildRole.CAPITÃO, GuildRole.SOLDADO }) {
            createUser(general.token(), "general-can-create-" + role.name(), role);
        }

        createUserExpecting(general.token(), "general-cannot-create-general", GuildRole.GENERAL, 403);
        createUserExpecting(general.token(), "general-cannot-create-marechal", GuildRole.MARECHAL, 403);
    }

    @Test
    void generalCanChangeRoleOnlyBelowGeneral() throws Exception {
        Actor owner = onboard("general-update-owner");
        Actor general = createActor(owner.token(), "general-update", GuildRole.GENERAL);
        Actor soldier = createActor(owner.token(), "general-update-soldier", GuildRole.SOLDADO);
        Actor otherGeneral = createActor(owner.token(), "general-update-other-general", GuildRole.GENERAL);

        updateRole(general.token(), soldier.id(), GuildRole.MAJOR).andExpect(status().isOk())
                .andExpect(jsonPath("$.guildRole").value("MAJOR"));
        updateRole(general.token(), otherGeneral.id(), GuildRole.MAJOR).andExpect(status().isForbidden());
        updateRole(general.token(), owner.id(), GuildRole.GENERAL).andExpect(status().isForbidden());
    }

    @Test
    void generalCanRemoveOnlyUsersBelowGeneral() throws Exception {
        Actor owner = onboard("general-delete-owner");
        Actor general = createActor(owner.token(), "general-delete", GuildRole.GENERAL);
        Actor soldier = createActor(owner.token(), "general-delete-soldier", GuildRole.SOLDADO);
        Actor otherGeneral = createActor(owner.token(), "general-delete-other-general", GuildRole.GENERAL);

        mockMvc.perform(delete("/users/" + soldier.id()).header("Authorization", bearer(general.token())))
                .andExpect(status().isNoContent());
        assertThat(userRepository.findById(soldier.id())).isEmpty();

        mockMvc.perform(delete("/users/" + otherGeneral.id()).header("Authorization", bearer(general.token())))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/users/" + owner.id()).header("Authorization", bearer(general.token())))
                .andExpect(status().isForbidden());
    }

    @Test
    void marechalCanExecuteElevatedRoleOperations() throws Exception {
        Actor owner = onboard("marechal-elevated-owner");
        Actor general = createActor(owner.token(), "marechal-elevated-general", GuildRole.GENERAL);
        Actor secondMarechal = createActor(owner.token(), "marechal-elevated-second", GuildRole.MARECHAL);

        updateRole(owner.token(), general.id(), GuildRole.MARECHAL).andExpect(status().isOk())
                .andExpect(jsonPath("$.guildRole").value("MARECHAL"));
        mockMvc.perform(delete("/users/" + secondMarechal.id()).header("Authorization", bearer(owner.token())))
                .andExpect(status().isNoContent());
        assertThat(userRepository.findById(secondMarechal.id())).isEmpty();
    }

    @Test
    void lastMarechalCannotBeRemovedOrDemoted() throws Exception {
        Actor owner = onboard("last-marechal-owner");

        mockMvc.perform(delete("/users/" + owner.id()).header("Authorization", bearer(owner.token())))
                .andExpect(status().isForbidden());
        updateRole(owner.token(), owner.id(), GuildRole.GENERAL).andExpect(status().isForbidden());
        assertThat(userRepository.findById(owner.id()).orElseThrow().getGuildRole()).isEqualTo(GuildRole.MARECHAL);
    }

    @Test
    void lastActiveMarechalCannotBeDisabledEvenWhenAnotherMarechalIsDisabled() throws Exception {
        Actor owner = onboard("last-active-marechal-owner");
        Actor second = createActor(owner.token(), "last-active-marechal-second", GuildRole.MARECHAL);
        userRepository.findById(second.id()).orElseThrow().setStatus(UserStatus.DISABLED);
        userRepository.flush();

        updateStatus(owner.token(), owner.id(), UserStatus.DISABLED).andExpect(status().isForbidden());
        assertThat(userRepository.findById(owner.id()).orElseThrow().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void statusLifecycleRevokesOldJwtAndAllowsFreshLogin() throws Exception {
        Actor owner = onboard("staff-status-owner");
        Actor general = createActor(owner.token(), "staff-status-general", GuildRole.GENERAL);
        Actor staff = createActor(owner.token(), "staff-status-soldier", GuildRole.SOLDADO);
        String originalHash = userRepository.findById(staff.id()).orElseThrow().getPassword();

        updateStatus(general.token(), staff.id(), UserStatus.DISABLED).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLED"))
                .andExpect(jsonPath("$.password").doesNotExist());
        assertThat(userRepository.findById(staff.id()).orElseThrow().getCredentialVersion()).isEqualTo(1L);
        mockMvc.perform(get("/users/me").header("Authorization", bearer(staff.token())))
                .andExpect(status().isUnauthorized());

        updateStatus(general.token(), staff.id(), UserStatus.ACTIVE).andExpect(status().isOk());
        assertThat(userRepository.findById(staff.id()).orElseThrow().getPassword()).isEqualTo(originalHash);
        mockMvc.perform(get("/users/me").header("Authorization", bearer(staff.token())))
                .andExpect(status().isUnauthorized());
        String renewed = login(staff.username());
        mockMvc.perform(get("/users/me").header("Authorization", bearer(renewed)))
                .andExpect(status().isOk());
    }

    @Test
    void statusChangesEnforceRoleTenantAndStateBoundaries() throws Exception {
        Actor ownerA = onboard("status-boundary-owner-a");
        Actor general = createActor(ownerA.token(), "status-boundary-general", GuildRole.GENERAL);
        Actor anotherGeneral = createActor(ownerA.token(), "status-boundary-other-general", GuildRole.GENERAL);
        Actor staff = createActor(ownerA.token(), "status-boundary-staff", GuildRole.SOLDADO);
        Actor ownerB = onboard("status-boundary-owner-b");

        updateStatus(general.token(), anotherGeneral.id(), UserStatus.DISABLED).andExpect(status().isForbidden());
        updateStatus(general.token(), ownerA.id(), UserStatus.DISABLED).andExpect(status().isForbidden());
        updateStatus(general.token(), general.id(), UserStatus.DISABLED).andExpect(status().isForbidden());
        updateStatus(ownerA.token(), ownerB.id(), UserStatus.DISABLED).andExpect(status().isNotFound());
        updateStatus(general.token(), staff.id(), UserStatus.PENDING).andExpect(status().isConflict());
        updateStatus(general.token(), staff.id(), UserStatus.ACTIVE).andExpect(status().isConflict());
        assertThat(userRepository.findById(staff.id()).orElseThrow().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void teamLeaderCannotBeDisabledUntilReassigned() throws Exception {
        Actor owner = onboard("team-leader-status-owner");
        Actor secondMarechal = createActor(owner.token(), "team-leader-status-second", GuildRole.MARECHAL);
        User leader = userRepository.findById(owner.id()).orElseThrow();
        Team team = teamRepository.save(Team.builder().name("Leader Team").guild(leader.getGuild()).leader(leader).build());

        updateStatus(secondMarechal.token(), owner.id(), UserStatus.DISABLED).andExpect(status().isConflict());
        assertThat(teamRepository.findById(team.getId()).orElseThrow().getLeader().getId()).isEqualTo(owner.id());
        assertThat(userRepository.findById(owner.id()).orElseThrow().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentStatusChangesCannotRemoveEveryActiveMarechal() throws Exception {
        String runId = UUID.randomUUID().toString().replace("-", "");
        Actor first = onboard("concurrent-marechal-first-" + runId);
        Actor second = createActor(first.token(), "concurrent-marechal-second-" + runId, GuildRole.MARECHAL);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> firstResult = executor.submit(() -> {
                start.await();
                return updateStatus(first.token(), second.id(), UserStatus.DISABLED).andReturn().getResponse().getStatus();
            });
            Future<Integer> secondResult = executor.submit(() -> {
                start.await();
                return updateStatus(second.token(), first.id(), UserStatus.DISABLED).andReturn().getResponse().getStatus();
            });
            start.countDown();
            int firstStatus = firstResult.get();
            int secondStatus = secondResult.get();

            assertThat(java.util.List.of(firstStatus, secondStatus)).contains(200);
            assertThat(java.util.List.of(firstStatus, secondStatus)).contains(403);
            assertThat(userRepository.countByGuildIdAndGuildRoleAndStatus(
                    userRepository.findById(first.id()).orElseThrow().getGuild().getId(), GuildRole.MARECHAL, UserStatus.ACTIVE))
                    .isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void userOperationsAreBlockedAcrossGuilds() throws Exception {
        Actor ownerA = onboard("cross-guild-owner-a");
        Actor generalA = createActor(ownerA.token(), "cross-guild-general-a", GuildRole.GENERAL);
        Actor ownerB = onboard("cross-guild-owner-b");

        mockMvc.perform(get("/users/" + ownerB.id()).header("Authorization", bearer(generalA.token())))
                .andExpect(status().isNotFound());
        updateRole(generalA.token(), ownerB.id(), GuildRole.GENERAL).andExpect(status().isNotFound());
        mockMvc.perform(delete("/users/" + ownerB.id()).header("Authorization", bearer(generalA.token())))
                .andExpect(status().isNotFound());
        assertThat(userRepository.findById(ownerB.id()).orElseThrow().getGuildRole()).isEqualTo(GuildRole.MARECHAL);
    }

    @Test
    void majorCaptainAndSoldierCannotManageUsers() throws Exception {
        Actor owner = onboard("lower-roles-owner");
        for (GuildRole role : new GuildRole[] { GuildRole.MAJOR, GuildRole.CAPITÃO, GuildRole.SOLDADO }) {
            Actor actor = createActor(owner.token(), "lower-roles-" + role.name(), role);
            mockMvc.perform(get("/users").header("Authorization", bearer(actor.token())))
                    .andExpect(status().isForbidden());
            createUserExpecting(actor.token(), "lower-roles-create-" + role.name(), GuildRole.SOLDADO, 403);
        }
    }

    @Test
    void regenerationRevokesPreviousTokenAndNewTokenActivatesPendingUser() throws Exception {
        Actor owner = onboard("activation-regeneration-owner");
        String pendingResponse = createPendingUser(owner.token(), "activation-pending", GuildRole.SOLDADO);
        long pendingUserId = pendingId(pendingResponse);
        String first = objectMapper.readTree(pendingResponse).get("activationToken").asText();

        String firstResponse = mockMvc.perform(post("/users/" + pendingUserId + "/activation")
                        .header("Authorization", bearer(owner.token())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String second = objectMapper.readTree(firstResponse).get("activationToken").asText();

        mockMvc.perform(post("/auth/activate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + first + "\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/activate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + second + "\",\"password\":\"password123\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/auth/activate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + second + "\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regenerationRespectsPendingStatusAndTenantBoundary() throws Exception {
        Actor ownerA = onboard("activation-boundary-owner-a");
        Actor ownerB = onboard("activation-boundary-owner-b");
        String pending = createPendingUser(ownerB.token(), "activation-boundary-pending", GuildRole.SOLDADO);
        long pendingId = pendingId(pending);

        mockMvc.perform(post("/users/" + pendingId + "/activation")
                        .header("Authorization", bearer(ownerA.token())))
                .andExpect(status().isNotFound());

        Actor active = createActor(ownerA.token(), "activation-active", GuildRole.SOLDADO);
        mockMvc.perform(post("/users/" + active.id() + "/activation")
                        .header("Authorization", bearer(ownerA.token())))
                .andExpect(status().isNotFound());

        String disabledResponse = createPendingUser(ownerA.token(), "activation-disabled", GuildRole.SOLDADO);
        long disabledId = pendingId(disabledResponse);
        userRepository.findById(disabledId).orElseThrow().setStatus(UserStatus.DISABLED);
        userRepository.flush();
        int tokensBefore = activationTokenRepository.findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(disabledId).size();
        mockMvc.perform(post("/users/" + disabledId + "/activation")
                        .header("Authorization", bearer(ownerA.token())))
                .andExpect(status().isNotFound());
        assertThat(activationTokenRepository.findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(disabledId))
                .hasSize(tokensBefore);
    }

    @Test
    void regenerationEnforcesGeneralHierarchyAndLowerRolesWithoutPartialMutation() throws Exception {
        Actor owner = onboard("activation-rbac-owner");
        Actor general = createActor(owner.token(), "activation-rbac-general", GuildRole.GENERAL);
        Actor otherGeneral = createActor(owner.token(), "activation-rbac-other-general", GuildRole.GENERAL);
        String pendingResponse = createPendingUser(owner.token(), "activation-rbac-pending", GuildRole.SOLDADO);
        long pendingId = pendingId(pendingResponse);

        String regenerated = mockMvc.perform(post("/users/" + pendingId + "/activation")
                        .header("Authorization", bearer(general.token())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String newToken = objectMapper.readTree(regenerated).get("activationToken").asText();
        assertThat(newToken).isNotEqualTo(objectMapper.readTree(pendingResponse).get("activationToken").asText());
        assertThat(activationTokenRepository.findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(pendingId)).hasSize(1);

        for (GuildRole role : new GuildRole[] { GuildRole.MAJOR, GuildRole.CAPITÃO, GuildRole.SOLDADO }) {
            Actor actor = createActor(owner.token(), "activation-rbac-actor-" + role.name(), role);
            int tokensBefore = activationTokenRepository.findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(pendingId).size();
            mockMvc.perform(post("/users/" + pendingId + "/activation")
                            .header("Authorization", bearer(actor.token())))
                    .andExpect(status().isForbidden());
            assertThat(activationTokenRepository.findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(pendingId))
                    .hasSize(tokensBefore);
        }

        for (Actor protectedTarget : new Actor[] { owner, otherGeneral }) {
            int tokensBefore = activationTokenRepository.findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(protectedTarget.id()).size();
            mockMvc.perform(post("/users/" + protectedTarget.id() + "/activation")
                            .header("Authorization", bearer(general.token())))
                    .andExpect(status().isForbidden());
            assertThat(activationTokenRepository.findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(protectedTarget.id()))
                    .hasSize(tokensBefore);
        }
    }

    @Test
    void regenerationPersistsOnlyTokenHash() throws Exception {
        Actor owner = onboard("activation-hash-owner");
        String pendingResponse = createPendingUser(owner.token(), "activation-hash-pending", GuildRole.SOLDADO);
        JsonNode creation = objectMapper.readTree(pendingResponse);
        String rawToken = creation.get("activationToken").asText();
        long userId = creation.get("id").asLong();

        String regenerated = mockMvc.perform(post("/users/" + userId + "/activation")
                        .header("Authorization", bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activationToken").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String regeneratedRaw = objectMapper.readTree(regenerated).get("activationToken").asText();

        String expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(regeneratedRaw.getBytes(StandardCharsets.UTF_8)));
        assertThat(activationTokenRepository.findAll()).extracting("tokenHash").contains(expectedHash)
                .doesNotContain(regeneratedRaw, rawToken);
    }

    @Test
    void invalidAndExpiredActivationTokensDoNotChangePendingAccount() throws Exception {
        Actor owner = onboard("activation-invalid-expired-owner");
        String pendingResponse = createPendingUser(owner.token(), "activation-invalid-expired", GuildRole.SOLDADO);
        JsonNode pending = objectMapper.readTree(pendingResponse);
        long userId = pending.get("id").asLong();
        String validToken = pending.get("activationToken").asText();

        mockMvc.perform(post("/auth/activate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"unknown-activation-token\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
        assertPendingWithoutPassword(userId);

        var persistedToken = activationTokenRepository.findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(userId)
                .get(0);
        persistedToken.setExpiresAt(Instant.now().minusSeconds(1));
        activationTokenRepository.saveAndFlush(persistedToken);

        mockMvc.perform(post("/auth/activate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + validToken + "\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
        assertPendingWithoutPassword(userId);
        assertThat(persistedToken.getUsedAt()).isNull();
    }

    private void assertPendingWithoutPassword(long userId) {
        User pending = userRepository.findById(userId).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(pending.getPassword()).isNull();
    }

    private String createPendingUser(String token, String username, GuildRole role) throws Exception {
        return createUserRequest(token, username, role).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private long pendingId(String response) throws Exception {
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void authenticatedUsersCanReadOnlyTheirOwnProfile() throws Exception {
        Actor owner = onboard("profile-owner");
        Actor general = createActor(owner.token(), "profile-general", GuildRole.GENERAL);
        Actor major = createActor(owner.token(), "profile-major", GuildRole.MAJOR);
        Actor captain = createActor(owner.token(), "profile-captain", GuildRole.CAPITÃO);
        Actor soldier = createActor(owner.token(), "profile-soldier", GuildRole.SOLDADO);

        for (Actor actor : new Actor[] { owner, general, major, captain, soldier }) {
            mockMvc.perform(get("/users/me").header("Authorization", bearer(actor.token())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(actor.username()))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }
    }

    @Test
    void teamAdministrativeEndpointsFollowTheRoleMatrix() throws Exception {
        Actor owner = onboard("team-rbac-owner");
        Actor major = createActor(owner.token(), "team-rbac-major", GuildRole.MAJOR);
        Actor soldier = createActor(owner.token(), "team-rbac-soldier", GuildRole.SOLDADO);
        User ownerUser = userRepository.findById(owner.id()).orElseThrow();
        Guild guild = ownerUser.getGuild();
        Team team = teamRepository.save(Team.builder().name("Team RBAC").guild(guild).leader(ownerUser).build());
        Member member = memberRepository.save(Member.builder().nickname("Member RBAC").phone("5511999999999")
                .guildRole(GuildRole.SOLDADO).rank("Recruit").classe(Classe.GUERREIRO).guild(guild).build());

        mockMvc.perform(post("/teams/" + team.getId() + "/add-member/" + member.getId())
                        .header("Authorization", bearer(major.token())))
                .andExpect(status().isOk());
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getTeam().getId()).isEqualTo(team.getId());

        mockMvc.perform(put("/teams/" + team.getId()).header("Authorization", bearer(soldier.token())))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/teams/" + team.getId() + "/add-member/" + member.getId())
                        .header("Authorization", bearer(soldier.token())))
                .andExpect(status().isForbidden());
    }

    @Test
    void teamLeaderCandidatesAreTenantScopedMinimalAndRestrictedToTeamAdmins() throws Exception {
        Actor ownerA = onboard("leader-candidates-owner-a");
        Actor major = createActor(ownerA.token(), "leader-candidates-major", GuildRole.MAJOR);
        Actor soldier = createActor(ownerA.token(), "leader-candidates-soldier", GuildRole.SOLDADO);
        Actor disabled = createActor(ownerA.token(), "leader-candidates-disabled", GuildRole.SOLDADO);
        long pending = createUser(ownerA.token(), "leader-candidates-pending", GuildRole.SOLDADO);
        Actor ownerB = onboard("leader-candidates-owner-b");
        userRepository.findById(disabled.id()).orElseThrow().setStatus(UserStatus.DISABLED);
        userRepository.flush();

        mockMvc.perform(get("/teams/leader-candidates").header("Authorization", bearer(major.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + ownerA.id() + ")].username").value(org.hamcrest.Matchers.hasItem(ownerA.username())))
                .andExpect(jsonPath("$[?(@.id == " + ownerB.id() + ")]").doesNotExist())
                .andExpect(jsonPath("$[?(@.id == " + disabled.id() + ")]").doesNotExist())
                .andExpect(jsonPath("$[?(@.id == " + pending + ")]").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].guildRole").doesNotExist())
                .andExpect(jsonPath("$[0].status").doesNotExist());
        mockMvc.perform(get("/teams/leader-candidates").header("Authorization", bearer(soldier.token())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/teams/leader-candidates")).andExpect(status().isUnauthorized());
    }

    @Test
    void teamLeaderCanBeReassignedThenOldLeaderCanBeDisabled() throws Exception {
        Actor owner = onboard("leader-reassign-owner");
        Actor secondMarechal = createActor(owner.token(), "leader-reassign-second-marechal", GuildRole.MARECHAL);
        Actor general = createActor(owner.token(), "leader-reassign-general", GuildRole.GENERAL);
        User ownerUser = userRepository.findById(owner.id()).orElseThrow();
        Team team = teamRepository.save(Team.builder().name("Transfer Team").guild(ownerUser.getGuild()).leader(ownerUser).build());

        mockMvc.perform(put("/teams/" + team.getId() + "/leader")
                        .header("Authorization", bearer(general.token())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leaderId\":" + general.id() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderId").value(general.id()))
                .andExpect(jsonPath("$.leaderName").value(general.username()));

        updateStatus(secondMarechal.token(), owner.id(), UserStatus.DISABLED).andExpect(status().isOk());
        assertThat(teamRepository.findById(team.getId()).orElseThrow().getLeader().getId()).isEqualTo(general.id());
    }

    @Test
    void teamLeaderReassignmentBlocksInactiveAndCrossGuildTargetsWithoutMutation() throws Exception {
        Actor ownerA = onboard("leader-boundary-owner-a");
        Actor generalA = createActor(ownerA.token(), "leader-boundary-general-a", GuildRole.GENERAL);
        Actor ownerB = onboard("leader-boundary-owner-b");
        long pendingId = createUser(ownerA.token(), "leader-boundary-pending", GuildRole.SOLDADO);
        User userA = userRepository.findById(ownerA.id()).orElseThrow();
        User userB = userRepository.findById(ownerB.id()).orElseThrow();
        Team teamA = teamRepository.save(Team.builder().name("A Team").guild(userA.getGuild()).leader(userA).build());
        Team teamB = teamRepository.save(Team.builder().name("B Team").guild(userB.getGuild()).leader(userB).build());
        userRepository.findById(generalA.id()).orElseThrow().setStatus(UserStatus.DISABLED);
        userRepository.flush();

        mockMvc.perform(put("/teams/" + teamA.getId() + "/leader").header("Authorization", bearer(ownerA.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"leaderId\":" + ownerB.id() + "}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/teams/" + teamB.getId() + "/leader").header("Authorization", bearer(ownerA.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"leaderId\":" + ownerA.id() + "}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/teams/" + teamA.getId() + "/leader").header("Authorization", bearer(ownerA.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"leaderId\":" + generalA.id() + "}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/teams/" + teamA.getId() + "/leader").header("Authorization", bearer(ownerA.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"leaderId\":" + pendingId + "}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/teams/" + teamA.getId() + "/leader").header("Authorization", bearer(ownerA.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/teams").header("Authorization", bearer(ownerA.token())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Invalid leader\",\"leaderId\":" + generalA.id() + "}"))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/teams").header("Authorization", bearer(ownerA.token())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cross Guild leader\",\"leaderId\":" + ownerB.id() + "}"))
                .andExpect(status().isNotFound());
        assertThat(teamRepository.findById(teamA.getId()).orElseThrow().getLeader().getId()).isEqualTo(ownerA.id());
        assertThat(teamRepository.findById(teamB.getId()).orElseThrow().getLeader().getId()).isEqualTo(ownerB.id());
    }

    @Test
    void onlyTeamAdminsCanReassignLeadersAndExtraFieldsCannotChangeUserOrGuild() throws Exception {
        Actor owner = onboard("leader-role-owner");
        Actor major = createActor(owner.token(), "leader-role-major", GuildRole.MAJOR);
        Actor captain = createActor(owner.token(), "leader-role-captain", GuildRole.CAPITÃO);
        Actor soldier = createActor(owner.token(), "leader-role-soldier", GuildRole.SOLDADO);
        User ownerUser = userRepository.findById(owner.id()).orElseThrow();
        Team team = teamRepository.save(Team.builder().name("Role Team").guild(ownerUser.getGuild()).leader(ownerUser).build());

        mockMvc.perform(put("/teams/" + team.getId() + "/leader").header("Authorization", bearer(major.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"leaderId\":" + major.id() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderId").value(major.id()));

        for (Actor denied : new Actor[] { captain, soldier }) {
            mockMvc.perform(put("/teams/" + team.getId() + "/leader").header("Authorization", bearer(denied.token()))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"leaderId\":" + denied.id() + "}"))
                    .andExpect(status().isForbidden());
        }
        mockMvc.perform(put("/teams/" + team.getId() + "/leader").header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leaderId\":" + captain.id() + ",\"guildId\":999999,\"role\":\"MARECHAL\",\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());
        assertThat(teamRepository.findById(team.getId()).orElseThrow().getLeader().getId()).isEqualTo(captain.id());
        assertThat(userRepository.findById(captain.id()).orElseThrow().getGuildRole()).isEqualTo(GuildRole.CAPITÃO);
        assertThat(userRepository.findById(captain.id()).orElseThrow().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentLeaderReassignmentAndDeactivationNeverLeavesDisabledLeaderAssigned() throws Exception {
        String runId = UUID.randomUUID().toString().replace("-", "");
        Actor owner = onboard("leader-race-owner-" + runId);
        Actor candidate = createActor(owner.token(), "leader-race-candidate-" + runId, GuildRole.GENERAL);
        User ownerUser = userRepository.findById(owner.id()).orElseThrow();
        Team team = teamRepository.save(Team.builder().name("Race Team").guild(ownerUser.getGuild()).leader(ownerUser).build());
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> reassignment = executor.submit(() -> {
                start.await();
                return mockMvc.perform(put("/teams/" + team.getId() + "/leader")
                                .header("Authorization", bearer(owner.token())).contentType(MediaType.APPLICATION_JSON)
                                .content("{\"leaderId\":" + candidate.id() + "}"))
                        .andReturn().getResponse().getStatus();
            });
            Future<Integer> deactivation = executor.submit(() -> {
                start.await();
                return updateStatus(owner.token(), candidate.id(), UserStatus.DISABLED)
                        .andReturn().getResponse().getStatus();
            });
            start.countDown();
            int reassignStatus = reassignment.get();
            int deactivateStatus = deactivation.get();

            assertThat(reassignStatus).isIn(200, 409);
            assertThat(deactivateStatus).isIn(200, 409);
            assertThat((reassignStatus == 200) ^ (deactivateStatus == 200)).isTrue();
            User persistedCandidate = userRepository.findById(candidate.id()).orElseThrow();
            Long persistedLeaderId = teamRepository.findById(team.getId()).orElseThrow().getLeader().getId();
            assertThat(persistedCandidate.getStatus() == UserStatus.DISABLED)
                    .isNotEqualTo(persistedLeaderId.equals(candidate.id()));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void adminControllerAcceptsAdministrativeRolesOnly() throws Exception {
        Actor owner = onboard("admin-controller-owner");
        Actor major = createActor(owner.token(), "admin-controller-major", GuildRole.MAJOR);
        Actor soldier = createActor(owner.token(), "admin-controller-soldier", GuildRole.SOLDADO);

        mockMvc.perform(get("/api/admin/restricted").header("Authorization", bearer(major.token())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/restricted").header("Authorization", bearer(soldier.token())))
                .andExpect(status().isForbidden());
    }

    private Actor onboard(String prefix) throws Exception {
        String username = prefix + "-owner";
        String response = mockMvc.perform(post("/auth/register-guild").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"guildName":"Guild %s","serverName":"Server","username":"%s","email":"%s@example.test","password":"password123"}
                                """.formatted(prefix, username, username)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new Actor(objectMapper.readTree(response).get("id").asLong(), username, login(username));
    }

    private Actor createActor(String token, String prefix, GuildRole role) throws Exception {
        String response = createUserRequest(token, prefix, role).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        mockMvc.perform(post("/auth/activate").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + json.get("activationToken").asText() + "\",\"password\":\"password123\"}"))
                .andExpect(status().isNoContent());
        long id = json.get("id").asLong();
        return new Actor(id, prefix, login(prefix));
    }

    private long createUser(String token, String username, GuildRole role) throws Exception {
        String response = createUserRequest(token, username, role).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void createUserExpecting(String token, String username, GuildRole role, int expectedStatus) throws Exception {
        createUserRequest(token, username, role).andExpect(status().is(expectedStatus));
    }

    private org.springframework.test.web.servlet.ResultActions createUserRequest(String token, String username,
            GuildRole role) throws Exception {
        String body = """
                {"username":"%s","email":"%s@example.test","password":"password123","role":"%s"}
                """.formatted(username, username, role.name());
        return mockMvc.perform(post("/users").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private org.springframework.test.web.servlet.ResultActions updateRole(String token, long id, GuildRole role)
            throws Exception {
        return mockMvc.perform(put("/users/" + id + "/role").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"" + role.name() + "\"}"));
    }

    private org.springframework.test.web.servlet.ResultActions updateStatus(String token, long id, UserStatus status)
            throws Exception {
        return mockMvc.perform(put("/users/" + id + "/status").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status.name() + "\"}"));
    }

    private String login(String username) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Actor(long id, String username, String token) { }
}
