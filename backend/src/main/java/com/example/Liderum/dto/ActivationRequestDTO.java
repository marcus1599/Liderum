package com.example.Liderum.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class ActivationRequestDTO { @NotBlank private String token; @NotBlank @Size(min=8,max=128) private String password; }
