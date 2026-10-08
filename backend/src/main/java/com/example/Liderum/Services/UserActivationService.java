package com.example.Liderum.Services;
import com.example.Liderum.Entities.User; import com.example.Liderum.dto.*;
public interface UserActivationService { UserActivationResponseDTO createActivation(User user); void activate(ActivationRequestDTO request); }
