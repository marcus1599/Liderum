package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Event;
import com.example.Liderum.Messaging.GuildEventCreatedMessage;
import com.example.Liderum.Messaging.GuildEventCreatedPublisher;
import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.Services.EventService;
import com.example.Liderum.dto.EventRequestDTO;
import com.example.Liderum.dto.EventResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final GuildEventCreatedPublisher guildEventCreatedPublisher;

    @Override
    public EventResponseDTO create(EventRequestDTO dto) {
        Event event = Event.builder()
                .name(dto.getName())
                .date(dto.getDate())
                .description(dto.getDescription())
                .build();

        Event saved = eventRepository.save(event);
        guildEventCreatedPublisher.publish(new GuildEventCreatedMessage(
                saved.getId(),
                saved.getName(),
                saved.getDate()
        ));

        return toResponseDTO(saved);
    }

    @Override
    public List<EventResponseDTO> findAll() {
        return eventRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDTO findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
        return toResponseDTO(event);
    }

    @Override
    public EventResponseDTO update(Long id, EventRequestDTO dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));

        event.setName(dto.getName());
        event.setDate(dto.getDate());
        event.setDescription(dto.getDescription());

        return toResponseDTO(eventRepository.save(event));
    }

    @Override
    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    private EventResponseDTO toResponseDTO(Event event) {
        return EventResponseDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .date(event.getDate())
                .description(event.getDescription())
                .build();
    }
}
