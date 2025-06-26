import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type AttendanceStatus = 'PRESENTE' | 'FALTOU' | 'JUSTIFICOU';

export interface Attendance {
  id?: number;
  memberId: number;
  eventId: number;
  status: AttendanceStatus;
}

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private apiUrl = 'http://localhost:8080/attendances';

  constructor(private http: HttpClient) {}

  getAttendances(): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(this.apiUrl);
  }

  createAttendance(attendance: Omit<Attendance, 'id'>): Observable<Attendance> {
    return this.http.post<Attendance>(this.apiUrl, attendance);
  }

  updateAttendance(id: number, attendance: Omit<Attendance, 'id'>): Observable<Attendance> {
    return this.http.put<Attendance>(`${this.apiUrl}/${id}`, attendance);
  }

  deleteAttendance(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getMembersWithConsecutiveAbsences(threshold: number) {
    return this.http.get<number[]>(`${this.apiUrl}/consecutive-absences?threshold=${threshold}`);
  }
}
