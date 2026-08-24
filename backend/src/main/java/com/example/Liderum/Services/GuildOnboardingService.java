package com.example.Liderum.Services;

import com.example.Liderum.dto.GuildRegistrationRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;

public interface GuildOnboardingService {
    UserResponseDTO register(GuildRegistrationRequestDTO request);
}
