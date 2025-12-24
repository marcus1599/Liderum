package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Team;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Repository.UserRepository;
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

    @InjectMocks
    private TeamServiceImpl teamService;

    private User leader;
    private Team team;

    @BeforeEach
    public void setUp() {
        leader = User.builder().id(1L).username("Marcus").build();
        team = Team.builder().id(1L).name("Guilda Top").leader(leader).build();
    }

    @Test
    public void shouldCreateTeamSuccessfully() {
        TeamRequestDTO request = new TeamRequestDTO();
        request.setName("Guilda Top");
        request.setLeaderId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
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

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> teamService.create(request));
    }

    @Test
    public void shouldReturnAllTeams() {
        when(teamRepository.findAll()).thenReturn(List.of(team));

        List<TeamResponseDTO> teams = teamService.findAll();

        assertEquals(1, teams.size());
        assertEquals("Guilda Top", teams.get(0).getName());
        assertEquals("Marcus", teams.get(0).getLeaderName());
    }

    @Test
    public void shouldFindTeamById() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        TeamResponseDTO dto = teamService.findById(1L);

        assertNotNull(dto);
        assertEquals("Guilda Top", dto.getName());
        assertEquals("Marcus", dto.getLeaderName());
    }

    @Test
    public void shouldDeleteTeam() {
        doNothing().when(teamRepository).deleteById(1L);

        teamService.delete(1L);

        verify(teamRepository, times(1)).deleteById(1L);
    }
}
