package com.example.Liderum.Controllers;

import com.example.Liderum.Services.MemberService;
import com.example.Liderum.dto.MemberRequestDTO;
import com.example.Liderum.dto.MemberResponseDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member management endpoints")
public class MemberController {

    private final MemberService memberService;

   
    
    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> findAll() {
        return ResponseEntity.ok(memberService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
    public ResponseEntity<MemberResponseDTO> create(@RequestBody @Valid MemberRequestDTO dto) {
    return ResponseEntity.ok(memberService.create(dto));
    }


    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }
    @PutMapping("/{id}")
@PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
public ResponseEntity<MemberResponseDTO> updateMember(@PathVariable Long id, @RequestBody @Valid MemberRequestDTO dto) {
    MemberResponseDTO updated = memberService.update(id, dto);
    return ResponseEntity.ok(updated);
}


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL', 'MAJOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
