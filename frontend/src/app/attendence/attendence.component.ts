import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MemberService } from '../services/member.service';
import { EventService } from '../services/event.service';
import { SettingsService } from '../services/settings.service';
import { Member } from '../members/member.model';
import { Event } from '../services/event.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { AttendanceService, AttendanceStatus, Attendance } from '../services/attendance.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize, forkJoin, Observable } from 'rxjs';
import { httpFeedback } from '../shared/http-feedback';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';
import { Subscription } from 'rxjs';
import { localLoadingContext } from '../core/interceptors/loading-context';

interface MemberFeedback {
  kind: 'success' | 'error';
  message: string;
}

@Component({
  selector: 'app-attendence',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, FormsModule, MatIconModule],
  templateUrl: './attendence.component.html',
  styleUrl: './attendence.component.scss'
})
export class AttendenceComponent implements OnInit, OnDestroy {
  members: Member[] = [];
  events: Event[] = [];
  attendances: Attendance[] = [];
  selectedEventId: number | null = null;
  membersWithAlert: number[] = [];
  attendancesLoaded = false;
  loading = false;
  loaded = false;
  pendingMemberIds = new Set<number>();
  memberFeedback = new Map<number, MemberFeedback>();
  alertLoading = false;
  alertLoaded = false;
  alertError = '';
  errorMessage = '';
  profile: UserProfile | null = null;
  private profileSubscription?: Subscription;
  private selectionRequest = 0;
  private alertRequest = 0;

  get selectedEvent(): Event | null {
    return this.events.find(event => event.id === this.selectedEventId) ?? null;
  }

  get saving(): boolean { return this.pendingMemberIds.size > 0; }

  constructor(
    private memberService: MemberService,
    private eventService: EventService,
    private settingsService: SettingsService,
    private attendanceService: AttendanceService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.profileSubscription = this.authService.profile$.subscribe(profile => this.profile = profile);
    this.loadData();
  }

  ngOnDestroy(): void { this.profileSubscription?.unsubscribe(); }

  canManageDomain(): boolean {
    return ['MARECHAL', 'GENERAL', 'MAJOR'].includes(this.profile?.guildRole ?? '');
  }

  loadData() {
    if (this.loading || this.saving) return;
    const request = ++this.selectionRequest;
    this.loading = true;
    this.loaded = false;
    this.errorMessage = '';
    forkJoin({
      members: this.memberService.getMembers(localLoadingContext()),
      events: this.eventService.getEvents(localLoadingContext()),
      attendances: this.attendanceService.getAttendances(localLoadingContext())
    }).subscribe({
      next: data => {
        if (request !== this.selectionRequest) return;
        this.members = data.members;
        this.events = data.events;
        this.attendances = this.normalize(data.attendances);
        this.loading = false;
        this.loaded = true;
        this.attendancesLoaded = this.selectedEventId !== null;
        this.memberFeedback.clear();
        this.loadAlerts();
      },
      error: (error: HttpErrorResponse) => {
        if (request !== this.selectionRequest) return;
        this.loading = false;
        this.attendancesLoaded = false;
        if (error.status === 401 || error.status === 403) {
          this.members = [];
          this.events = [];
          this.attendances = [];
          this.membersWithAlert = [];
        }
        this.errorMessage = httpFeedback(error, 'consultar presenças', true);
      }
    });
  }

  private normalize(attendances: Attendance[]): Attendance[] {
    return attendances.map(a => ({ ...a, eventId: Number(a.eventId), memberId: Number(a.memberId) }));
  }

  loadAlerts(): void {
    const request = ++this.alertRequest;
    this.alertLoading = true;
    this.alertLoaded = false;
    this.alertError = '';
    this.attendanceService.getMembersWithConsecutiveAbsences(this.settingsService.maxAbsences, localLoadingContext()).subscribe({
      next: ids => {
        if (request !== this.alertRequest) return;
        this.membersWithAlert = ids;
        this.alertLoading = false;
        this.alertLoaded = true;
      },
      error: (error: HttpErrorResponse) => {
        if (request !== this.alertRequest) return;
        this.alertLoading = false;
        this.membersWithAlert = [];
        if (error.status === 401 || error.status === 403) {
          this.loaded = false;
          this.members = [];
          this.events = [];
          this.attendances = [];
        }
        this.alertError = httpFeedback(error, 'consultar alertas de faltas', true);
      }
    });
  }

  markAttendance(memberId: number, status: AttendanceStatus, input?: HTMLInputElement) {
    if (!this.canManageDomain() || this.selectedEventId == null || !this.loaded || this.loading || this.pendingMemberIds.has(memberId)) return;
    const eventId = this.selectedEventId;
    const existing = this.attendances.find(a => a.memberId === memberId && a.eventId === eventId);
    const confirmed = existing?.status ?? null;
    const attendance: Omit<Attendance, 'id'> = { memberId, eventId, status };
    this.pendingMemberIds.add(memberId);
    this.memberFeedback.delete(memberId);
    this.restoreConfirmedSelection(input, confirmed);
    const operation: Observable<Attendance> = existing?.id
      ? this.attendanceService.updateAttendance(existing.id, attendance, localLoadingContext())
      : this.attendanceService.createAttendance(attendance, localLoadingContext());
    operation.pipe(finalize(() => this.pendingMemberIds.delete(memberId))).subscribe({
      next: saved => {
        this.attendances = [...this.attendances.filter(a => !(a.memberId === memberId && a.eventId === eventId)),
          ...this.normalize([saved])];
        this.memberFeedback.set(memberId, { kind: 'success', message: 'Presença salva.' });
        this.loadAlerts();
      },
      error: (error: HttpErrorResponse) => {
        this.memberFeedback.set(memberId, { kind: 'error', message: httpFeedback(error, 'salvar presença') });
        this.restoreConfirmedSelection(input, confirmed);
      }
    });
  }

  private restoreConfirmedSelection(input: HTMLInputElement | undefined, confirmed: AttendanceStatus | null): void {
    input?.closest('tr')?.querySelectorAll<HTMLInputElement>('input[type="radio"]').forEach(radio => {
      radio.checked = radio.value === confirmed;
    });
  }

  checkConsecutiveAbsences(memberId: number): boolean {
    const sortedEvents = [...this.events].sort((a, b) => a.date.localeCompare(b.date));
    const lastEvents = sortedEvents.slice(-this.settingsService.maxAbsences);
    return lastEvents.every(ev => {
      const att = this.attendances.find(a => a.memberId === memberId && a.eventId === ev.id);
      return att && att.status === 'FALTOU';
    });
  }

  getStatus(memberId: number): AttendanceStatus | null {
    if (this.selectedEventId == null) return null;
    const att = this.attendances.find(a => a.memberId === memberId && a.eventId === this.selectedEventId);
    return att ? att.status : null;
  }

  hasAttendancesForSelectedEvent(): boolean {
    return this.selectedEventId !== null && this.attendances.some(a => a.eventId === this.selectedEventId);
  }

  onEventChange(eventId: any) {
    if (this.saving || !this.loaded) return;
    this.memberFeedback.clear();
    const parsedEventId = Number(eventId);
    this.selectedEventId = eventId === null || eventId === undefined || eventId === '' || eventId === 'null'
      ? null
      : Number.isInteger(parsedEventId) && parsedEventId > 0 ? parsedEventId : null;
    this.attendancesLoaded = false;
    this.errorMessage = '';
    if (this.selectedEventId === null) return;
    const request = ++this.selectionRequest;
    this.loading = true;
    this.attendanceService.getAttendances(localLoadingContext()).subscribe({ next: att => {
      if (request !== this.selectionRequest) return;
      this.attendances = this.normalize(att);
      this.attendancesLoaded = true;
      this.loading = false;
    }, error: (error: HttpErrorResponse) => {
      if (request !== this.selectionRequest) return;
      this.loading = false;
      this.attendancesLoaded = false;
      if (error.status === 401 || error.status === 403) {
        this.loaded = false;
        this.attendances = [];
      }
      this.errorMessage = httpFeedback(error, 'consultar presenças', true);
    }});
  }

  getMemberNickname(memberId: number): string {
    const member = this.members.find(m => m.id === memberId);
    return member ? member.nickname : 'ID ' + memberId;
  }

  isChecked(memberId: number, status: AttendanceStatus): boolean {
    if (this.selectedEventId == null) return false;
    const found = this.attendances.find(
      a => Number(a.memberId) === Number(memberId) && Number(a.eventId) === Number(this.selectedEventId) && a.status === status
    );
    return !!found;
  }
}
