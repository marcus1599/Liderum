import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject } from 'rxjs';
import { EventsComponent } from './events.component';
import { Event, EventService } from '../services/event.service';

describe('EventsComponent HTTP feedback', () => {
  let fixture: ComponentFixture<EventsComponent>;
  let component: EventsComponent;
  let service: jasmine.SpyObj<EventService>;

  beforeEach(async () => {
    service = jasmine.createSpyObj<EventService>('EventService', ['getEvents', 'createEvent', 'updateEvent', 'deleteEvent']);
    service.getEvents.and.returnValue(of([]));
    await TestBed.configureTestingModule({
      imports: [EventsComponent], providers: [{ provide: EventService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(EventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('does not present an API failure as an empty list and retry clears it', () => {
    service.getEvents.and.returnValue(new Subject<Event[]>());
    component.loadEvents();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Carregando eventos');
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum evento cadastrado');
    (service.getEvents.calls.mostRecent().returnValue as Subject<Event[]>).error(new HttpErrorResponse({ status: 404 }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('não está disponível');
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum evento cadastrado');
    service.getEvents.and.returnValue(of([]));
    component.loadEvents();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Nenhum evento cadastrado');
  });

  it('keeps the event form after a failed write and blocks duplicate submit', () => {
    const pending = new Subject<Event>();
    service.createEvent.and.returnValue(pending);
    component.newEvent = { name: 'Raid', date: '2026-10-15T20:00', description: '' };
    component.saveEvent();
    component.saveEvent();
    expect(service.createEvent).toHaveBeenCalledTimes(1);
    pending.error(new HttpErrorResponse({ status: 0 }));
    expect(component.newEvent.name).toBe('Raid');
    expect(component.saving).toBeFalse();
    expect(component.errorMessage).toContain('Atualize a lista');
  });

  it('reports successful persistence even when the following read fails', () => {
    service.createEvent.and.returnValue(of({ id: 9, name: 'Raid', date: '2026-10-15T20:00' }));
    service.getEvents.and.returnValue(new Subject<Event[]>());
    component.saveEvent();
    expect(component.statusMessage).toBe('Evento criado.');
    (service.getEvents.calls.mostRecent().returnValue as Subject<Event[]>).error(new HttpErrorResponse({ status: 500 }));
    expect(component.errorMessage).toContain('Operação concluída');
    expect(component.loaded).toBeFalse();
  });
});
