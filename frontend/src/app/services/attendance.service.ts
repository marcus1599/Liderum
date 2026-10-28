import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type AttendanceStatus = 'PRESENTE' | 'FALTOU' | 'JUSTIFICOU';

export interface Attendance {
  id?: number;
  memberId: number;
  eventId: number;
  status: AttendanceStatus;
  memberName?: string;
  eventName?: string;
}

@Injectable({ providedIn: 'root' })
export class AttendanceService {

  private apiUrl = `${environment.apiUrl}/attendances`;

  constructor(private http: HttpClient) {}

  getAttendances(context?: HttpContext): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(this.apiUrl, { context });
  }

  createAttendance(attendance: Omit<Attendance, 'id'>, context?: HttpContext): Observable<Attendance> {
    return this.http.post<Attendance>(this.apiUrl, attendance, { context });
  }

  updateAttendance(id: number, attendance: Omit<Attendance, 'id'>, context?: HttpContext): Observable<Attendance> {
    return this.http.put<Attendance>(`${this.apiUrl}/${id}`, attendance, { context });
  }

  deleteAttendance(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getMembersWithConsecutiveAbsences(threshold: number, context?: HttpContext) {
    return this.http.get<number[]>(`${this.apiUrl}/consecutive-absences?threshold=${threshold}`, { context });
  }
}
