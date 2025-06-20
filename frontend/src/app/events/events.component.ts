import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventService, Event } from '../services/event.service';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatButtonModule, MatInputModule],
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.scss']
})
export class EventsComponent implements OnInit {
  events: Event[] = [];
  newEvent: Partial<Event> = { name: '', date: '', description: '' };
  isEditing: boolean = false;
  editingEventId: number | null = null;

  constructor(private eventService: EventService) {}

  ngOnInit() {
    this.loadEvents();
  }

  loadEvents() {
    this.eventService.getEvents().subscribe(events => this.events = events);
  }

  saveEvent() {
    if (this.isEditing && this.editingEventId !== null) {
      this.eventService.updateEvent({ ...this.newEvent, id: this.editingEventId } as Event).subscribe(() => {
        this.loadEvents();
        this.resetForm();
      });
    } else {
      this.eventService.createEvent(this.newEvent).subscribe(() => {
        this.loadEvents();
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
