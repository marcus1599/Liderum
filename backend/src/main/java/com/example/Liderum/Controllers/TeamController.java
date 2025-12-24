package com.example.Liderum.Controllers;

import com.example.Liderum.Services.TeamService;
import com.example.Liderum.dto.MemberRequestDTO;
import com.example.Liderum.dto.TeamRequestDTO;
import com.example.Liderum.dto.TeamResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Endpoints para gerenciamento de equipes")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
    @Operation(summary = "Criar uma nova equipe")
    public ResponseEntity<TeamResponseDTO> create(@Valid @RequestBody TeamRequestDTO request) {
        return ResponseEntity.ok(teamService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas as equipes")
    public ResponseEntity<List<TeamResponseDTO>> findAll() {
        return ResponseEntity.ok(teamService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar uma equipe por ID")
    public ResponseEntity<TeamResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
    @Operation(summary = "Excluir uma equipe por ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{teamId}/add-member/{memberId}")
    @Operation(summary = "Adicionar um membro a uma equipe")
    public ResponseEntity<Void> addMemberToTeam(@PathVariable Long teamId, @PathVariable Long memberId) {
        teamService.addMemberToTeam(teamId, memberId);
        System.out.println("Member added to team: " + teamId + ", Member ID: " + memberId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{teamId}")
    @Operation(summary = "Atualizar a  equipe")
public ResponseEntity<Void> update(@PathVariable Long teamId) {
    teamService.update(teamId);
    return ResponseEntity.ok().build();
}

@DeleteMapping("/{teamId}/remove-member/{memberId}")
@PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
@Operation(summary = "Remover um membro de uma equipe")
public ResponseEntity<Void> removeMemberFromTeam(@PathVariable Long teamId, @PathVariable Long memberId) {
    teamService.removeMemberFromTeam(teamId, memberId);
    return ResponseEntity.ok().build();
}

}
