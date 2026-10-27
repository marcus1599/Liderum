import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
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
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule,MatIcon, MatButtonModule, MatInputModule],
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.scss']
})
export class EventsComponent implements OnInit, OnDestroy {
  @ViewChild('eventFormHeading') private eventFormHeading?: ElementRef<HTMLElement>;
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
  profile: UserProfile | null = null;
  private profileSubscription?: Subscription;

  constructor(private eventService: EventService, private authService: AuthService) {}

  ngOnInit() {
    this.profileSubscription = this.authService.profile$.subscribe(profile => this.profile = profile);
    this.loadEvents();
  }

  ngOnDestroy(): void { this.profileSubscription?.unsubscribe(); }

  canManageDomain(): boolean {
    return ['MARECHAL', 'GENERAL', 'MAJOR'].includes(this.profile?.guildRole ?? '');
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
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
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
    if (!this.canManageDomain()) return;
    this.newEvent = { ...event };
    this.isEditing = true;
    this.editingEventId = event.id;
    this.eventFormHeading?.nativeElement.focus();
  }

  deleteEvent(id: number) {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
    const eventName = this.events.find(event => event.id === id)?.name ?? 'selecionado';
    if (!confirm(`Tem certeza que deseja excluir o evento "${eventName}"?`)) return;
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
