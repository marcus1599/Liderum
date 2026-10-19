import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendenceComponent } from './attendence.component';
import { MemberService } from '../services/member.service';
import { EventService } from '../services/event.service';
import { AttendanceService } from '../services/attendance.service';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { Member } from '../members/member.model';
import { Event } from '../services/event.service';
import { Attendance } from '../services/attendance.service';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';

describe('AttendenceComponent', () => {
  let component: AttendenceComponent;
  let fixture: ComponentFixture<AttendenceComponent>;
  let members: jasmine.SpyObj<MemberService>;
  let events: jasmine.SpyObj<EventService>;
  let attendance: jasmine.SpyObj<AttendanceService>;
  let profile$: BehaviorSubject<UserProfile | null>;
  const hero = { id: 1, nickname: 'Hero' } as Member;
  const eventOne: Event = { id: 1, name: 'Raid', date: '2026-10-15T20:00:00' };
  const eventTwo: Event = { id: 2, name: 'Quest', date: '2026-10-16T20:00:00' };

  beforeEach(async () => {
    members = jasmine.createSpyObj<MemberService>('MemberService', ['getMembers']);
    events = jasmine.createSpyObj<EventService>('EventService', ['getEvents']);
    attendance = jasmine.createSpyObj<AttendanceService>('AttendanceService',
      ['getAttendances', 'getMembersWithConsecutiveAbsences', 'createAttendance', 'updateAttendance']);
    members.getMembers.and.returnValue(of([hero]));
    events.getEvents.and.returnValue(of([eventOne, eventTwo]));
    attendance.getAttendances.and.returnValue(of([]));
    attendance.getMembersWithConsecutiveAbsences.and.returnValue(of([]));
    profile$ = new BehaviorSubject<UserProfile | null>({ id: 1, username: 'admin', email: 'admin@example.test', guildRole: 'MARECHAL' });
    await TestBed.configureTestingModule({
      imports: [AttendenceComponent],
      providers: [
        { provide: MemberService, useValue: members },
        { provide: EventService, useValue: events },
        { provide: AttendanceService, useValue: attendance },
        { provide: AuthService, useValue: { profile$: profile$.asObservable() } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttendenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('shows confirmed attendance read-only and blocks mutations for a low role', () => {
    component.selectedEventId = 1;
    component.loaded = true;
    component.attendancesLoaded = true;
    component.attendances = [{ id: 3, memberId: 1, eventId: 1, status: 'PRESENTE' }];
    profile$.next({ id: 2, username: 'soldier', email: 'soldier@example.test', guildRole: 'SOLDADO' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('input[type="radio"]').length).toBe(0);
    expect(fixture.nativeElement.textContent).toContain('PRESENTE');
    component.markAttendance(1, 'FALTOU');
    expect(attendance.updateAttendance).not.toHaveBeenCalled();
    expect(attendance.createAttendance).not.toHaveBeenCalled();
  });

  it('allows attendance administration only after resolving an authorized profile', () => {
    profile$.next(null);
    expect(component.canManageDomain()).toBeFalse();
    for (const guildRole of ['MARECHAL', 'GENERAL', 'MAJOR'] as const) {
      profile$.next({ id: 1, username: 'admin', email: 'admin@example.test', guildRole });
      expect(component.canManageDomain()).toBeTrue();
    }
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('waits for all essential reads and reports their failure without a false empty state', () => {
    const pending = new Subject<Member[]>();
    members.getMembers.and.returnValue(pending);
    component.loadData();
    fixture.detectChanges();
    expect(component.loading).toBeTrue();
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum membro cadastrado');
    pending.error(new HttpErrorResponse({ status: 403 }));
    fixture.detectChanges();
    expect(component.loaded).toBeFalse();
    expect(component.errorMessage).toContain('permissão');
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum membro cadastrado');
  });

  it('keeps alert failures separate from saved attendance data', () => {
    attendance.getMembersWithConsecutiveAbsences.and.returnValue(new Subject<number[]>());
    component.loadData();
    (attendance.getMembersWithConsecutiveAbsences.calls.mostRecent().returnValue as Subject<number[]>).error(
      new HttpErrorResponse({ status: 500 }));
    fixture.detectChanges();
    expect(component.loaded).toBeTrue();
    expect(component.alertLoaded).toBeFalse();
    expect(component.alertError).toContain('carregar os dados');
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum membro em risco');
  });

  it('ignores an older event response when the selection changes', () => {
    const first = new Subject<Attendance[]>();
    const second = new Subject<Attendance[]>();
    attendance.getAttendances.and.returnValues(first, second);
    component.onEventChange(1);
    component.onEventChange(2);
    second.next([{ id: 8, memberId: 1, eventId: 2, status: 'PRESENTE' }]);
    second.complete();
    first.next([{ id: 7, memberId: 1, eventId: 1, status: 'FALTOU' }]);
    first.complete();
    expect(component.selectedEventId).toBe(2);
    expect(component.attendances[0].eventId).toBe(2);
    expect(component.loading).toBeFalse();
  });

  it('allows the first attendance when the selected event has no records', () => {
    component.onEventChange(1);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Ainda não há presenças neste evento');
    expect(fixture.nativeElement.querySelectorAll('input[type="radio"]').length).toBe(3);
  });

  it('restores the confirmed radio after a failed write and prevents another write pending', () => {
    component.selectedEventId = 1;
    component.attendancesLoaded = true;
    component.attendances = [{ id: 3, memberId: 1, eventId: 1, status: 'PRESENTE' }];
    fixture.detectChanges();
    const radios = fixture.nativeElement.querySelectorAll('input[type="radio"]') as NodeListOf<HTMLInputElement>;
    const pending = new Subject<Attendance>();
    attendance.updateAttendance.and.returnValue(pending);
    radios[1].checked = true;
    component.markAttendance(1, 'FALTOU', radios[1]);
    component.markAttendance(1, 'JUSTIFICOU', radios[2]);
    expect(attendance.updateAttendance).toHaveBeenCalledTimes(1);
    pending.error(new HttpErrorResponse({ status: 0 }));
    expect(radios[0].checked).toBeTrue();
    expect(radios[1].checked).toBeFalse();
    expect(component.attendances[0].status).toBe('PRESENTE');
  });

  it('keeps saved attendance confirmed if the refresh fails', () => {
    component.selectedEventId = 1;
    component.attendancesLoaded = true;
    attendance.createAttendance.and.returnValue(of({ id: 4, memberId: 1, eventId: 1, status: 'PRESENTE' }));
    attendance.getAttendances.and.returnValue(new Subject<Attendance[]>());
    component.markAttendance(1, 'PRESENTE');
    expect(component.statusMessage).toBe('Presença salva.');
    (attendance.getAttendances.calls.mostRecent().returnValue as Subject<Attendance[]>).error(
      new HttpErrorResponse({ status: 500 }));
    expect(component.errorMessage).toContain('Presença salva, mas a lista não foi atualizada');
    expect(component.loaded).toBeFalse();
  });
});
