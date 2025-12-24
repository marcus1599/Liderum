package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Event;
import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.dto.EventRequestDTO;
import com.example.Liderum.dto.EventResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventRequestDTO request;
    private Event event;

    @BeforeEach
    void setup() {
        request = EventRequestDTO.builder()
                .name("Missão Principal")
                .date(LocalDateTime.of(2025, 4, 10, 20, 0))
                .description("Evento de ataque ao castelo")
                .build();

        event = Event.builder()
                .id(1L)
                .name(request.getName())
                .date(request.getDate())
                .description(request.getDescription())
                .build();
    }

    @Test
    void shouldCreateEventSuccessfully() {
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventResponseDTO response = eventService.create(request);

        assertNotNull(response);
        assertEquals("Missão Principal", response.getName());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void shouldFindAllEvents() {
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<EventResponseDTO> result = eventService.findAll();

        assertEquals(1, result.size());
        assertEquals("Missão Principal", result.get(0).getName());
    }

    @Test
    void shouldFindEventById() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventResponseDTO result = eventService.findById(1L);

        assertEquals("Missão Principal", result.getName());
    }

    @Test
    void shouldUpdateEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventRequestDTO updateRequest = EventRequestDTO.builder()
                .name("Missão Atualizada")
                .date(LocalDateTime.of(2025, 4, 15, 21, 0))
                .description("Nova descrição")
                .build();

        EventResponseDTO result = eventService.update(1L, updateRequest);

        assertEquals("Missão Atualizada", result.getName());
        assertEquals("Nova descrição", result.getDescription());
    }

    @Test
    void shouldDeleteEvent() {
        doNothing().when(eventRepository).deleteById(1L);

        eventService.delete(1L);

        verify(eventRepository, times(1)).deleteById(1L);
    }
}
