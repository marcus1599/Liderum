import { Injectable } from '@angular/core';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AttendanceService, Attendance } from './attendance.service';
import { MemberService } from './member.service';
import { EventService, Event } from './event.service';
import { Member } from '../members/member.model';

export interface DashboardMetrics {
  presencePie: { status: string, count: number }[];
  presenceLine: { event: string, presentes: number, faltas: number, justificados: number }[];
  classPie: { classe: string, count: number }[];
  highlights: { member: Member, presencas: number }[];
  alerts: { member: Member, faltasConsecutivas: number }[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(
    private attendanceService: AttendanceService,
    private memberService: MemberService,
    private eventService: EventService
  ) {}

  getDashboardMetrics(eventCount: number): Observable<DashboardMetrics> {
    return forkJoin({
      attendances: this.attendanceService.getAttendances(),
      members: this.memberService.getMembers(),
      events: this.eventService.getEvents()
    }).pipe(
      map(({ attendances, members, events }) => {
        // Ordena eventos por data decrescente e pega os mais recentes
        const sortedEvents = [...events].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
        const recentEvents = sortedEvents.slice(0, eventCount);

        // Filtra presenças só dos eventos recentes
        const recentAttendances = attendances.filter(a => recentEvents.some(e => e.id === a.eventId));

        // 1. Gráfico de pizza de presença geral
        const statusCounts = { PRESENTE: 0, FALTOU: 0, JUSTIFICOU: 0 };
        recentAttendances.forEach(a => statusCounts[a.status] = (statusCounts[a.status] || 0) + 1);
        const presencePie = [
          { status: 'Presente', count: statusCounts.PRESENTE },
          { status: 'Faltou', count: statusCounts.FALTOU },
          { status: 'Justificou', count: statusCounts.JUSTIFICOU }
        ];

        // 2. Gráfico de linha de presença por evento
        const presenceLine = recentEvents.map(event => {
          const eventAttendances = recentAttendances.filter(a => a.eventId === event.id);
          return {
            event: event.name,
            presentes: eventAttendances.filter(a => a.status === 'PRESENTE').length,
            faltas: eventAttendances.filter(a => a.status === 'FALTOU').length,
            justificados: eventAttendances.filter(a => a.status === 'JUSTIFICOU').length
          };
        }).reverse(); // Para mostrar do mais antigo ao mais recente

        // 3. Gráfico de pizza de distribuição por classe
        const classMap: { [classe: string]: number } = {};
        members.forEach(m => {
          classMap[m.classe] = (classMap[m.classe] || 0) + 1;
        });
        const classPie = Object.entries(classMap).map(([classe, count]) => ({ classe, count }));

        // 4. Destaques positivos: membros com mais presenças
        const memberPresenceCount: { [id: number]: number } = {};
        recentAttendances.forEach(a => {
          if (a.status === 'PRESENTE') {
            memberPresenceCount[a.memberId] = (memberPresenceCount[a.memberId] || 0) + 1;
          }
        });
        const highlights = members
          .map(m => ({ member: m, presencas: memberPresenceCount[m.id] || 0 }))
          .sort((a, b) => b.presencas - a.presencas)
          .slice(0, 5);

        // 5. Alertas: membros com mais de 3 faltas consecutivas
        const alerts: { member: Member, faltasConsecutivas: number }[] = [];
        members.forEach(m => {
          // Ordena as presenças do membro por data do evento
          const memberAttendances = recentEvents.map(e => 
            recentAttendances.find(a => a.memberId === m.id && a.eventId === e.id)
          );
          let consecutivas = 0;
          for (const att of memberAttendances) {
            if (att && att.status === 'FALTOU') consecutivas++;
            else consecutivas = 0;
          }
          if (consecutivas >= 3) {
            alerts.push({ member: m, faltasConsecutivas: consecutivas });
          }
        });

        return { presencePie, presenceLine, classPie, highlights, alerts };
      })
    );
  }
}