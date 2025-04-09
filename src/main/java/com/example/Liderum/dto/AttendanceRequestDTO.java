package com.example.Liderum.dto;

import com.example.Liderum.Enums.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceRequestDTO {

    @NotNull(message = "O ID do membro é obrigatório")
    private Long memberId;

    @NotNull(message = "O ID do evento é obrigatório")
    private Long eventId;

    @NotNull(message = "O status da presença é obrigatório")
    private AttendanceStatus status;
}
