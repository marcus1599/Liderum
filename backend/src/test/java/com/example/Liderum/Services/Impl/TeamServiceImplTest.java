package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.TeamRequestDTO;
import com.example.Liderum.dto.TeamResponseDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {
    @Mock TeamRepository teamRepository;
    @Mock MemberRepository memberRepository;
    @Mock TenantService tenantService;
    @Mock EntityManager entityManager;
    @InjectMocks TeamServiceImpl teamService;

    private Guild guild;
    private Team team;
    private Member member;

    @BeforeEach
    void setUp() {
        guild = Guild.builder().id(1L).name("Guild Test").build();
        team = Team.builder().id(2L).name("Team A").guild(guild).build();
        member = Member.builder().id(3L).nickname("Tactical leader").guild(guild).team(team).build();
        team.setLeader(member);
        lenient().when(tenantService.getCurrentGuild()).thenReturn(guild);
        lenient().when(tenantService.getCurrentGuildId()).thenReturn(1L);
    }

    @Test
    void newTeamsHaveNoLeader() {
        when(teamRepository.save(any(Team.class))).thenAnswer(call -> call.getArgument(0));
        TeamResponseDTO response = teamService.create(new TeamRequestDTO());
        assertNull(response.getLeaderMemberId());
        assertNull(response.getLeaderNickname());
        verify(teamRepository).save(argThat(saved -> saved.getLeader() == null));
    }

    @Test
    void returnsOnlyMembersForRequestedTeamAsCandidates() {
        when(teamRepository.findByIdAndGuildId(2L, 1L)).thenReturn(Optional.of(team));
        when(memberRepository.findAllByTeamAndGuild(2L, 1L)).thenReturn(List.of(member));
        var candidates = teamService.findLeaderCandidates(2L);
        assertEquals(1, candidates.size());
        assertEquals(3L, candidates.get(0).id());
        assertEquals("Tactical leader", candidates.get(0).nickname());
    }

    @Test
    void reassignsLeadershipToMemberWithoutAccountDependency() {
        when(memberRepository.findByIdAndGuildIdForUpdate(3L, 1L)).thenReturn(Optional.of(member));
        when(teamRepository.findByIdAndGuildIdForUpdate(2L, 1L)).thenReturn(Optional.of(team));
        when(teamRepository.save(team)).thenReturn(team);
        var result = teamService.reassignLeader(2L, 3L);
        assertEquals(3L, result.getLeaderMemberId());
        assertEquals("Tactical leader", result.getLeaderNickname());
        verifyNoInteractions(entityManager);
    }

    @Test
    void rejectsLeaderMemberNotAssignedToTargetTeam() {
        Member outsider = Member.builder().id(4L).nickname("Outsider").guild(guild).build();
        when(memberRepository.findByIdAndGuildIdForUpdate(4L, 1L)).thenReturn(Optional.of(outsider));
        when(teamRepository.findByIdAndGuildIdForUpdate(2L, 1L)).thenReturn(Optional.of(team));
        assertThrows(EntityNotFoundException.class, () -> teamService.reassignLeader(2L, 4L));
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void movingCurrentLeaderClearsSourceTeamBeforeMembershipChanges() {
        Team destination = Team.builder().id(5L).name("Team B").guild(guild).build();
        when(memberRepository.findByIdAndGuildIdForUpdate(3L, 1L)).thenReturn(Optional.of(member));
        when(teamRepository.findAllByIdsAndGuildIdForUpdate(List.of(2L, 5L), 1L)).thenReturn(List.of(team, destination));
        teamService.addMemberToTeam(5L, 3L);
        assertNull(team.getLeader());
        assertEquals(destination, member.getTeam());
        verify(teamRepository).saveAndFlush(team);
        verify(memberRepository).saveAndFlush(member);
    }

    @Test
    void removingCurrentLeaderClearsTeamMarker() {
        when(memberRepository.findByIdAndGuildIdForUpdate(3L, 1L)).thenReturn(Optional.of(member));
        when(teamRepository.findAllByIdsAndGuildIdForUpdate(List.of(2L), 1L)).thenReturn(List.of(team));
        teamService.removeMemberFromTeam(2L, 3L);
        assertNull(team.getLeader());
        assertNull(member.getTeam());
        verify(teamRepository).saveAndFlush(team);
    }

    @Test
    void deletingTeamFlushesLeaderClearBeforeCascadeDelete() {
        when(memberRepository.findAllByTeamAndGuildForUpdate(2L, 1L)).thenReturn(List.of(member));
        when(teamRepository.findByIdAndGuildIdForUpdate(2L, 1L)).thenReturn(Optional.of(team));
        teamService.delete(2L);
        assertNull(team.getLeader());
        verify(teamRepository).saveAndFlush(team);
        verify(teamRepository).delete(team);
    }
}
