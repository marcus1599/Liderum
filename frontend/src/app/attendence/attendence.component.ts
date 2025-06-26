import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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

@Component({
  selector: 'app-attendence',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatFormFieldModule, MatInputModule, FormsModule, MatIconModule],
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

  constructor(
    private memberService: MemberService,
    private eventService: EventService,
    private settingsService: SettingsService,
    private attendanceService: AttendanceService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.memberService.getMembers().subscribe(members => this.members = members);
    this.eventService.getEvents().subscribe(events => this.events = events);
    this.attendanceService.getAttendances().subscribe(att => this.attendances = att);
    this.attendanceService.getMembersWithConsecutiveAbsences(this.settingsService.maxAbsences)
      .subscribe(ids => this.membersWithAlert = ids);
  }

  markAttendance(memberId: number, status: AttendanceStatus) {
    if (this.selectedEventId == null) return;
    const existing = this.attendances.find(a => a.memberId === memberId && a.eventId === this.selectedEventId);
    const attendance: Omit<Attendance, 'id'> = { memberId, eventId: this.selectedEventId, status };
    const refreshAttendances = () => {
      this.attendanceService.getAttendances().subscribe(att => {
        this.attendances = att.map(a => ({
          ...a,
          eventId: Number(a.eventId),
          memberId: Number(a.memberId)
        }));
        this.attendanceService.getMembersWithConsecutiveAbsences(this.settingsService.maxAbsences)
          .subscribe(ids => {
            this.membersWithAlert = ids;
            this.cdr.detectChanges();
          });
      });
    };
    if (existing && existing.id) {
      this.attendanceService.updateAttendance(existing.id, attendance).subscribe(a => {
        // Atualiza localmente com o retorno do backend
        Object.assign(existing, a);
        
        refreshAttendances();
      });
    } else {
      this.attendanceService.createAttendance(attendance).subscribe(a => {
        // Remove qualquer presença local sem id (otimista) e adiciona a retornada do backend
        this.attendances = this.attendances.filter(att => !(att.memberId === memberId && att.eventId === this.selectedEventId && att.id === undefined));
        this.attendances.push({
          ...a,
          eventId: Number(a.eventId),
          memberId: Number(a.memberId)
        });
       
        this.cdr.detectChanges();
        refreshAttendances();
      });
    }
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

  onEventChange(eventId: any) {
    this.selectedEventId = eventId ? Number(eventId) : null;
    this.attendancesLoaded = false;
    this.attendanceService.getAttendances().subscribe(att => {
      this.attendances = att.map(a => ({
        ...a,
        eventId: Number(a.eventId),
        memberId: Number(a.memberId)
      }));
      this.attendancesLoaded = true;
      this.attendanceService.getMembersWithConsecutiveAbsences(this.settingsService.maxAbsences)
        .subscribe(ids => {
          this.membersWithAlert = ids;
          this.cdr.detectChanges();
        });
    });
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
