package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.TeamRequestDTO;
import com.example.Liderum.dto.TeamResponseDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Guild guild;
    private User leader;
    private Team team;

    @BeforeEach
    public void setUp() {
        guild = Guild.builder().id(1L).name("Guilda Teste").build();
        leader = User.builder().id(1L).username("Marcus").guild(guild).build();
        team = Team.builder().id(1L).name("Guilda Top").leader(leader).guild(guild).build();

        lenient().when(tenantService.getCurrentGuild()).thenReturn(guild);
        lenient().when(tenantService.getCurrentGuildId()).thenReturn(1L);
    }

    @Test
    public void shouldCreateTeamSuccessfully() {
        TeamRequestDTO request = new TeamRequestDTO();
        request.setName("Guilda Top");
        request.setLeaderId(1L);

        when(userRepository.findByIdAndGuildId(1L, 1L)).thenReturn(Optional.of(leader));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamResponseDTO response = teamService.create(request);

        assertNotNull(response);
        assertEquals("Guilda Top", response.getName());
        assertEquals("Marcus", response.getLeaderName());
    }

    @Test
    public void shouldThrowWhenLeaderNotFound() {
        TeamRequestDTO request = new TeamRequestDTO();
        request.setName("Guilda Teste");
        request.setLeaderId(99L);

        when(userRepository.findByIdAndGuildId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> teamService.create(request));
    }

    @Test
    public void shouldReturnAllTeams() {
        when(teamRepository.findAllByGuildId(1L)).thenReturn(List.of(team));

        List<TeamResponseDTO> teams = teamService.findAll();

        assertEquals(1, teams.size());
        assertEquals("Guilda Top", teams.get(0).getName());
        assertEquals("Marcus", teams.get(0).getLeaderName());
    }

    @Test
    public void shouldFindTeamById() {
        when(teamRepository.findByIdAndGuildId(1L, 1L)).thenReturn(Optional.of(team));

        TeamResponseDTO dto = teamService.findById(1L);

        assertNotNull(dto);
        assertEquals("Guilda Top", dto.getName());
        assertEquals("Marcus", dto.getLeaderName());
    }

    @Test
    public void shouldDeleteTeam() {
        when(teamRepository.findByIdAndGuildId(1L, 1L)).thenReturn(Optional.of(team));
        doNothing().when(teamRepository).delete(team);

        teamService.delete(1L);

        verify(teamRepository, times(1)).delete(team);
    }
}
