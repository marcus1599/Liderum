package com.example.Liderum.Controllers;

import com.example.Liderum.Services.EventService;
import com.example.Liderum.dto.EventRequestDTO;
import com.example.Liderum.dto.EventResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gerencia os eventos da guilda")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MARSHAL', 'GENERAL', 'MAJOR')")
    @Operation(summary = "Criar novo evento")
    public ResponseEntity<EventResponseDTO> create(@RequestBody @Valid EventRequestDTO dto) {
        EventResponseDTO created = eventService.create(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @Operation(summary = "Listar todos os eventos")
    public ResponseEntity<List<EventResponseDTO>> findAll() {
        return ResponseEntity.ok(eventService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    public ResponseEntity<EventResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MARSHAL', 'GENERAL', 'MAJOR')")
    @Operation(summary = "Atualizar evento")
    public ResponseEntity<EventResponseDTO> update(@PathVariable Long id,
                                                   @RequestBody @Valid EventRequestDTO dto) {
        return ResponseEntity.ok(eventService.update(id, dto));
    }

    @DeleteMapping("/{id}")
      @PreAuthorize("hasAnyRole('MARSHAL', 'GENERAL', 'MAJOR')")
    @Operation(summary = "Deletar evento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
