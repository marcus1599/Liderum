import { Component, OnInit } from '@angular/core';
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
import { forkJoin, Observable } from 'rxjs';
import { httpFeedback } from '../shared/http-feedback';

@Component({
  selector: 'app-attendence',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, FormsModule, MatIconModule],
  templateUrl: './attendence.component.html',
  styleUrl: './attendence.component.scss'
})
export class AttendenceComponent implements OnInit {
  members: Member[] = [];
  events: Event[] = [];
  attendances: Attendance[] = [];
  selectedEventId: number | null = null;
  membersWithAlert: number[] = [];
  attendancesLoaded = false;
  loading = false;
  loaded = false;
  saving = false;
  alertLoading = false;
  alertLoaded = false;
  alertError = '';
  statusMessage = '';
  stale = false;
  errorMessage = '';
  private selectionRequest = 0;
  private alertRequest = 0;

  constructor(
    private memberService: MemberService,
    private eventService: EventService,
    private settingsService: SettingsService,
    private attendanceService: AttendanceService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData(afterMutation = false) {
    if (this.loading) return;
    if (!afterMutation) this.statusMessage = '';
    const request = ++this.selectionRequest;
    this.loading = true;
    this.loaded = false;
    this.errorMessage = '';
    forkJoin({
      members: this.memberService.getMembers(),
      events: this.eventService.getEvents(),
      attendances: this.attendanceService.getAttendances()
    }).subscribe({
      next: data => {
        if (request !== this.selectionRequest) return;
        this.members = data.members;
        this.events = data.events;
        this.attendances = this.normalize(data.attendances);
        this.loading = false;
        this.loaded = true;
        this.stale = false;
        this.attendancesLoaded = this.selectedEventId !== null;
        this.loadAlerts();
      },
      error: (error: HttpErrorResponse) => {
        if (request !== this.selectionRequest) return;
        this.loading = false;
        this.stale = afterMutation;
        this.attendancesLoaded = false;
        if (error.status === 401 || error.status === 403) {
          this.members = [];
          this.events = [];
          this.attendances = [];
          this.membersWithAlert = [];
        }
        this.errorMessage = afterMutation
          ? `Presença salva, mas os dados não foram atualizados. ${httpFeedback(error, 'consultar presenças', true)}`
          : httpFeedback(error, 'consultar presenças', true);
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
    this.attendanceService.getMembersWithConsecutiveAbsences(this.settingsService.maxAbsences).subscribe({
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
    if (this.selectedEventId == null || !this.loaded || this.loading || this.saving) return;
    const eventId = this.selectedEventId;
    const existing = this.attendances.find(a => a.memberId === memberId && a.eventId === eventId);
    const confirmed = existing?.status ?? null;
    const attendance: Omit<Attendance, 'id'> = { memberId, eventId, status };
    this.saving = true;
    this.errorMessage = '';
    this.statusMessage = '';
    const operation: Observable<Attendance> = existing?.id
      ? this.attendanceService.updateAttendance(existing.id, attendance)
      : this.attendanceService.createAttendance(attendance);
    operation.subscribe({
      next: saved => {
        this.saving = false;
        this.statusMessage = 'Presença salva.';
        this.attendances = [...this.attendances.filter(a => !(a.memberId === memberId && a.eventId === eventId)),
          ...this.normalize([saved])];
        this.refreshAttendancesAfterSave();
        this.loadAlerts();
      },
      error: (error: HttpErrorResponse) => {
        this.saving = false;
        this.errorMessage = httpFeedback(error, 'salvar presença');
        if (input) {
          input.closest('tr')?.querySelectorAll<HTMLInputElement>('input[type="radio"]').forEach(radio => {
            radio.checked = radio.value === confirmed;
          });
        }
      }
    });
  }

  private refreshAttendancesAfterSave(): void {
    const request = ++this.selectionRequest;
    this.loading = true;
    this.attendanceService.getAttendances().subscribe({
      next: att => {
        if (request !== this.selectionRequest) return;
        this.attendances = this.normalize(att);
        this.loading = false;
        this.loaded = true;
        this.stale = false;
        this.attendancesLoaded = this.selectedEventId !== null;
      },
      error: (error: HttpErrorResponse) => {
        if (request !== this.selectionRequest) return;
        this.loading = false;
        this.loaded = false;
        this.stale = true;
        this.attendancesLoaded = false;
        if (error.status === 401 || error.status === 403) this.attendances = [];
        this.errorMessage = `Presença salva, mas a lista não foi atualizada. ${httpFeedback(error, 'consultar presenças', true)}`;
      }
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
    this.statusMessage = '';
    this.selectedEventId = eventId ? Number(eventId) : null;
    this.attendancesLoaded = false;
    this.errorMessage = '';
    if (this.selectedEventId === null) return;
    const request = ++this.selectionRequest;
    this.loading = true;
    this.attendanceService.getAttendances().subscribe({ next: att => {
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
