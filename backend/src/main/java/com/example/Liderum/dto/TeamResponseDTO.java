package com.example.Liderum.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamResponseDTO {
    private Long id;
    private String name;
    private String leaderName;
}
