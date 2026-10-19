package com.example.Liderum.dto;

import jakarta.validation.constraints.NotNull;

public record TeamLeaderUpdateRequestDTO(@NotNull Long leaderId) {}
