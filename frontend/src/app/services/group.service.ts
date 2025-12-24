import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Member } from '../members/member.model';

export interface Group {
  id: number;
  name: string;
  members: Member[];
}

@Injectable({ providedIn: 'root' })
export class GroupService {
  private apiUrl = 'http://localhost:8080/teams';
  private membersUrl = 'http://localhost:8080/api/members';

  constructor(private http: HttpClient) {}

  getGroups(): Observable<Group[]> {
    return this.http.get<Group[]>(this.apiUrl);
  }

  createGroup(group: Partial<Group>): Observable<Group> {
    return this.http.post<Group>(this.apiUrl, group);
  }

  updateGroup(group: Group): Observable<Group> {
    return this.http.put<Group>(`${this.apiUrl}/${group.id}`, group);
  }

  deleteGroup(groupId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${groupId}`);
  }

  getMembers(): Observable<Member[]> {
    return this.http.get<Member[]>(this.membersUrl);
  }

  // Adiciona membro ao grupo
  addMemberToGroup(groupId: number, memberId: number): Observable<Group> {
    return this.http.post<Group>(`${this.apiUrl}/${groupId}/add-member/${memberId}`, {});
  }

  // Remove membro do grupo
  removeMemberFromGroup(groupId: number, memberId: number): Observable<Group> {
    return this.http.delete<Group>(`${this.apiUrl}/${groupId}/remove-member/${memberId}`);
  }
}
