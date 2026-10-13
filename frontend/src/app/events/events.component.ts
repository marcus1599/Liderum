import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventService, Event } from '../services/event.service';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { HttpErrorResponse } from '@angular/common/http';
import { httpFeedback } from '../shared/http-feedback';
import { Observable } from 'rxjs';

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
  loaded = false;
  saving = false;
  stale = false;
  errorMessage = '';
  statusMessage = '';

  constructor(private eventService: EventService) {}

  ngOnInit() {
    this.loadEvents();
  }

  loadEvents(afterMutation = false) {
    if (this.loading) return;
    if (!afterMutation) this.statusMessage = '';
    this.loading = true;
    this.errorMessage = '';
    this.eventService.getEvents().subscribe({
      next: events => { this.events = events; this.loading = false; this.loaded = true; this.stale = false; },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.loaded = false;
        this.stale = afterMutation;
        if (error.status === 401 || error.status === 403) this.events = [];
        this.errorMessage = afterMutation
          ? `Operação concluída, mas a lista não foi atualizada. ${httpFeedback(error, 'consultar eventos', true)}`
          : httpFeedback(error, 'consultar eventos', true);
      }
    });
  }

  saveEvent() {
    if (this.saving || this.loading || !this.loaded) return;
    this.saving = true;
    this.errorMessage = '';
    this.statusMessage = '';
    // Converte o valor do input datetime-local para formato ISO LocalDateTime (yyyy-MM-ddTHH:mm:ss)
    const request = { ...this.newEvent };
    if (request.date) {
      const dateValue = request.date;
      // Se vier como string do input, pode estar no formato 'yyyy-MM-ddTHH:mm'
      if (typeof dateValue === 'string' && dateValue.length === 16) {
        request.date = dateValue + ':00'; // adiciona os segundos
      }
    }
    const editing = this.isEditing && this.editingEventId !== null;
    const operation: Observable<Event> = editing
      ? this.eventService.updateEvent({ ...request, id: this.editingEventId } as Event)
      : this.eventService.createEvent(request);
    operation.subscribe({
      next: () => {
        this.saving = false;
        this.statusMessage = editing ? 'Evento atualizado.' : 'Evento criado.';
        this.resetForm();
        this.loadEvents(true);
      },
      error: (error: HttpErrorResponse) => { this.saving = false; this.errorMessage = httpFeedback(error, 'salvar evento'); }
    });
  }

  editEvent(event: Event) {
    this.newEvent = { ...event };
    this.isEditing = true;
    this.editingEventId = event.id;
  }

  deleteEvent(id: number) {
    if (this.saving || this.loading || !this.loaded || !confirm('Tem certeza que deseja excluir este evento?')) return;
    this.saving = true;
    this.errorMessage = '';
    this.statusMessage = '';
    this.eventService.deleteEvent(id).subscribe({
      next: () => { this.saving = false; this.statusMessage = 'Evento excluído.'; this.loadEvents(true); },
      error: (error: HttpErrorResponse) => { this.saving = false; this.errorMessage = httpFeedback(error, 'excluir evento'); }
    });
  }

  resetForm() {
    this.newEvent = { name: '', date: '', description: '' };
    this.isEditing = false;
    this.editingEventId = null;
  }
}
