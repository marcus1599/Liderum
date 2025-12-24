import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Event {
  id: number;
  name: string;
  date: string;
  description?: string;
}

@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = 'http://localhost:8080/api/events';

  constructor(private http: HttpClient) {}

  getEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(this.apiUrl);
  }

  getEvent(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  createEvent(event: Partial<Event>): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, event);
  }

  updateEvent(event: Event): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/${event.id}`, event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
