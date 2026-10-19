import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { EventsComponent } from './events.component';
import { Event, EventService } from '../services/event.service';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';

describe('EventsComponent HTTP feedback', () => {
  let fixture: ComponentFixture<EventsComponent>;
  let component: EventsComponent;
  let service: jasmine.SpyObj<EventService>;
  let profile$: BehaviorSubject<UserProfile | null>;

  beforeEach(async () => {
    service = jasmine.createSpyObj<EventService>('EventService', ['getEvents', 'createEvent', 'updateEvent', 'deleteEvent']);
    service.getEvents.and.returnValue(of([]));
    profile$ = new BehaviorSubject<UserProfile | null>({ id: 1, username: 'admin', email: 'admin@example.test', guildRole: 'MARECHAL' });
    await TestBed.configureTestingModule({
      imports: [EventsComponent], providers: [
        { provide: EventService, useValue: service },
        { provide: AuthService, useValue: { profile$: profile$.asObservable() } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(EventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('keeps event reading and hides/blocks mutations for a read-only role', () => {
    component.events = [{ id: 3, name: 'Raid', date: '2026-10-15T20:00:00' }];
    component.loaded = true;
    profile$.next({ id: 2, username: 'captain', email: 'captain@example.test', guildRole: 'CAPITÃO' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Raid');
    expect(fixture.nativeElement.textContent).not.toContain('Novo Evento');
    expect(fixture.nativeElement.querySelector('[aria-label="Editar evento"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('[aria-label="Excluir evento"]')).toBeNull();
    component.saveEvent();
    component.editEvent(component.events[0]);
    component.deleteEvent(3);
    expect(service.createEvent).not.toHaveBeenCalled();
    expect(service.updateEvent).not.toHaveBeenCalled();
    expect(service.deleteEvent).not.toHaveBeenCalled();
  });

  it('keeps domain administration for MARECHAL, GENERAL and MAJOR and hides it while profile is unresolved', () => {
    profile$.next(null);
    fixture.detectChanges();
    expect(component.canManageDomain()).toBeFalse();
    for (const guildRole of ['MARECHAL', 'GENERAL', 'MAJOR'] as const) {
      profile$.next({ id: 1, username: 'admin', email: 'admin@example.test', guildRole });
      expect(component.canManageDomain()).toBeTrue();
    }
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
