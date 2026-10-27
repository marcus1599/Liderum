import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Member } from '../members/member.model';
import { environment } from '../../environments/environment';

export interface Group {
  id: number;
  name: string;
  leaderMemberId?: number | null;
  leaderNickname?: string | null;
  members: Member[];
}

export interface TeamRequest { name: string; leaderMemberId?: number | null; }
export interface TeamLeaderCandidate { id: number; nickname: string; }

@Injectable({ providedIn: 'root' })
export class GroupService {
  private apiUrl = environment.apiUrl + '/teams';
  private membersUrl = environment.apiUrl + '/members';

  constructor(private http: HttpClient) {}

  getGroups(): Observable<Group[]> {
    return this.http.get<Group[]>(this.apiUrl);
  }

  createGroup(group: TeamRequest): Observable<Group> {
    return this.http.post<Group>(this.apiUrl, group);
  }

  updateGroup(groupId: number, request: TeamRequest): Observable<Group> {
    return this.http.put<Group>(`${this.apiUrl}/${groupId}`, request);
  }

  getLeaderCandidates(teamId: number): Observable<TeamLeaderCandidate[]> {
    return this.http.get<TeamLeaderCandidate[]>(`${this.apiUrl}/${teamId}/leader-candidates`);
  }

  reassignLeader(teamId: number, leaderMemberId: number): Observable<Group> {
    return this.http.put<Group>(`${this.apiUrl}/${teamId}/leader`, { leaderMemberId });
  }

  deleteGroup(groupId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${groupId}`);
  }

  getMembers(): Observable<Member[]> {
    return this.http.get<Member[]>(this.membersUrl);
  }

  // Adiciona membro ao grupo
  addMemberToGroup(groupId: number, memberId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${groupId}/add-member/${memberId}`, {});
  }

  // Remove membro do grupo
  removeMemberFromGroup(groupId: number, memberId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${groupId}/remove-member/${memberId}`);
  }
}
