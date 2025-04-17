package com.example.Liderum.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EventResponseDTO {
    private Long id;
    private String name;
    private LocalDateTime date;
    private String description;
}
