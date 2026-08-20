package com.example.Liderum.Tenancy;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.GuildRepository;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Services.MemberService;
import com.example.Liderum.Services.TeamService;
import com.example.Liderum.dto.MemberRequestDTO;
import com.example.Liderum.dto.TeamRequestDTO;
import com.example.Liderum.exceptions.MemberNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "jwt.secret=test_only_multi_tenant_isolation_secret")
@Transactional
class MultiTenantIsolationIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private GuildRepository guildRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Guild guildA;
    private Guild guildB;
    private User userA;
    private User userB;
    private Team teamA;
    private Team teamB;
    private Member memberA;
    private Member memberB;

    @BeforeEach
    void setUp() {
        guildA = guildRepository.save(Guild.builder().name("Guild A").serverName("server-a").build());
        guildB = guildRepository.save(Guild.builder().name("Guild B").serverName("server-b").build());

        userA = userRepository.save(user("user-a", "user-a@example.test", guildA));
        userB = userRepository.save(user("user-b", "user-b@example.test", guildB));

        teamA = teamRepository.save(Team.builder().name("Team A").guild(guildA).leader(userA).build());
        teamB = teamRepository.save(Team.builder().name("Team B").guild(guildB).leader(userB).build());

        memberA = memberRepository.save(member("member-a", guildA, teamA));
        memberB = memberRepository.save(member("member-b", guildB, teamB));

        authenticateAs(userA);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldListOnlyMembersFromAuthenticatedGuild() {
        var members = memberService.findAll();

        assertThat(members).extracting(member -> member.getId())
                .containsExactly(memberA.getId());
    }

    @Test
    void shouldRejectCrossGuildMemberReadUpdateAndDelete() {
        MemberRequestDTO update = memberRequest("changed-member", teamA.getId());

        assertThatThrownBy(() -> memberService.findById(memberB.getId()))
                .isInstanceOf(MemberNotFoundException.class);
        assertThatThrownBy(() -> memberService.update(memberB.getId(), update))
                .isInstanceOf(MemberNotFoundException.class);
        assertThatThrownBy(() -> memberService.delete(memberB.getId()))
                .isInstanceOf(MemberNotFoundException.class);

        assertThat(memberRepository.findById(memberB.getId())).isPresent();
        assertThat(memberRepository.findById(memberB.getId()).orElseThrow().getNickname())
                .isEqualTo("member-b");
    }

    @Test
    void shouldRejectCrossGuildTeamReferenceWhenCreatingMember() {
        MemberRequestDTO request = memberRequest("new-member", teamB.getId());

        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Team not found");

        assertThat(memberRepository.findAllByGuildId(guildA.getId()))
                .extracting(Member::getNickname)
                .containsExactly("member-a");
    }

    @Test
    void shouldRestrictTeamReadsAndChangesToAuthenticatedGuild() {
        assertThat(teamService.findAll()).extracting(team -> team.getId())
                .containsExactly(teamA.getId());
        assertThatThrownBy(() -> teamService.findById(teamB.getId()))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> teamService.delete(teamB.getId()))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(teamRepository.findById(teamB.getId())).isPresent();
    }

    @Test
    void shouldRejectCrossGuildLeaderWhenCreatingTeam() {
        TeamRequestDTO request = new TeamRequestDTO();
        request.setName("Invalid cross-guild team");
        request.setLeaderId(userB.getId());

        assertThatThrownBy(() -> teamService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Lider nao encontrado");

        assertThat(teamRepository.findAllByGuildId(guildA.getId()))
                .extracting(Team::getName)
                .containsExactly("Team A");
    }

    @Test
    void shouldRejectUnauthenticatedOrGuildlessTenantAccess() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(tenantService::getCurrentGuild)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authenticated user is required.");

        User guildlessUser = userRepository.save(user("guildless", "guildless@example.test", null));
        authenticateAs(guildlessUser);

        assertThatThrownBy(tenantService::getCurrentGuild)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authenticated user is not linked to a guild.");
    }

    private User user(String username, String email, Guild guild) {
        return User.builder()
                .username(username)
                .email(email)
                .password("test-password")
                .guildRole(GuildRole.MARECHAL)
                .guild(guild)
                .build();
    }

    private Member member(String nickname, Guild guild, Team team) {
        return Member.builder()
                .nickname(nickname)
                .phone("5511999999999")
                .guildRole(GuildRole.SOLDADO)
                .rank("Recruit")
                .classe(Classe.GUERREIRO)
                .guild(guild)
                .team(team)
                .build();
    }

    private MemberRequestDTO memberRequest(String nickname, Long teamId) {
        MemberRequestDTO request = new MemberRequestDTO();
        request.setNickname(nickname);
        request.setPhone("5511999999999");
        request.setGuildRole(GuildRole.SOLDADO);
        request.setRank("Recruit");
        request.setClasse(Classe.GUERREIRO);
        request.setTeamId(teamId);
        return request;
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MARECHAL"))
                )
        );
    }
}
