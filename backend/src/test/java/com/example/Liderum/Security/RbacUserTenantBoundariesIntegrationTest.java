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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

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
