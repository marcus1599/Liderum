package com.example.Liderum.Services;

import com.example.Liderum.dto.EventRequestDTO;
import com.example.Liderum.dto.EventResponseDTO;

import java.util.List;

public interface EventService {
    EventResponseDTO create(EventRequestDTO dto);
    List<EventResponseDTO> findAll();
    EventResponseDTO findById(Long id);
    EventResponseDTO update(Long id, EventRequestDTO dto);
    void delete(Long id);
}
