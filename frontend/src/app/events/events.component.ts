import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventService, Event } from '../services/event.service';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule,MatIcon, MatButtonModule, MatInputModule],
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.scss']
})
export class EventsComponent implements OnInit {
  events: Event[] = [];
  newEvent: Partial<Event> = { name: '', date: '', description: '' };
  isEditing: boolean = false;
  editingEventId: number | null = null;
  loading = false;
  errorMessage = '';
  statusMessage = '';

  constructor(private eventService: EventService) {}

  ngOnInit() {
    this.loadEvents();
  }

  loadEvents() {
    this.loading = true;
    this.eventService.getEvents().subscribe({ next: events => { this.events = events; this.loading = false; }, error: (error: HttpErrorResponse) => { this.loading = false; this.errorMessage = error.status === 403 ? 'Você não tem permissão para consultar eventos.' : error.status === 404 ? 'Eventos não encontrados.' : 'Não foi possível carregar os eventos.'; } });
  }

  saveEvent() {
    // Converte o valor do input datetime-local para formato ISO LocalDateTime (yyyy-MM-ddTHH:mm:ss)
    if (this.newEvent.date) {
      const dateValue = this.newEvent.date;
      // Se vier como string do input, pode estar no formato 'yyyy-MM-ddTHH:mm'
      if (typeof dateValue === 'string' && dateValue.length === 16) {
        this.newEvent.date = dateValue + ':00'; // adiciona os segundos
      }
    }
    if (this.isEditing && this.editingEventId !== null) {
      this.eventService.updateEvent({ ...this.newEvent, id: this.editingEventId } as Event).subscribe(() => {
        this.loadEvents(); this.statusMessage = 'Evento atualizado.';
        this.resetForm();
      });
    } else {
      this.eventService.createEvent(this.newEvent).subscribe(() => {
        this.loadEvents(); this.statusMessage = 'Evento criado.';
        this.resetForm();
      });
    }
  }

  editEvent(event: Event) {
    this.newEvent = { ...event };
    this.isEditing = true;
    this.editingEventId = event.id;
  }

  deleteEvent(id: number) {
    if (confirm('Tem certeza que deseja excluir este evento?')) {
      this.eventService.deleteEvent(id).subscribe(() => this.loadEvents());
    }
  }

  resetForm() {
    this.newEvent = { name: '', date: '', description: '' };
    this.isEditing = false;
    this.editingEventId = null;
  }
}
