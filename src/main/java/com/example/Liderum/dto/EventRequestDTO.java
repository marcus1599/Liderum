package com.example.Liderum.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EventRequestDTO {

    @NotBlank(message = "O nome do evento é obrigatório")
    private String name;

    @NotNull(message = "A data do evento é obrigatória")
    private LocalDateTime date;

    private String description;
}
