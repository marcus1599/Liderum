package com.example.Liderum.Controllers;

import com.example.Liderum.Services.AttendanceService;
import com.example.Liderum.dto.AttendanceRequestDTO;
import com.example.Liderum.dto.AttendanceResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Gerencia presenças em eventos")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
    @Operation(summary = "Registra a presença de um membro em um evento")
    public ResponseEntity<AttendanceResponseDTO> create(@RequestBody @Valid AttendanceRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.create(dto));
    }

    @GetMapping
    @Operation(summary = "Lista todas as presenças registradas")
    public ResponseEntity<List<AttendanceResponseDTO>> findAll() {
        return ResponseEntity.ok(attendanceService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma presença específica por ID")
    public ResponseEntity<AttendanceResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
    @Operation(summary = "Atualiza o status de uma presença")
    public ResponseEntity<AttendanceResponseDTO> update(@PathVariable Long id, @RequestBody @Valid AttendanceRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
      @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
    @Operation(summary = "Remove um registro de presença")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consecutive-absences")
    @Operation(summary = "Retorna membros com N faltas consecutivas")
    public ResponseEntity<List<Long>> getMembersWithConsecutiveAbsences(@RequestParam int threshold) {
        return ResponseEntity.ok(attendanceService.findMembersWithConsecutiveAbsences(threshold));
    }
}
