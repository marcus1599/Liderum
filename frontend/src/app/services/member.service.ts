import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Member } from '../members/member.model';

@Injectable({
  providedIn: 'root'
})
export class MemberService {

  private apiUrl = 'http://localhost:8080/api/members'; 

  constructor(private http: HttpClient) { }

  getMembers(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);  
  }

  deleteMember(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

 
  addMember(member: Member): Observable<Member> {
    return this.http.post<any>(this.apiUrl, member);
  }
  updateMember(member: Member): Observable<Member> {
    return this.http.put<Member>(`${this.apiUrl}/${member.id}`, member);
  }
}
