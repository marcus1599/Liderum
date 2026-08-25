import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CreateUserRequest, ManagedUser, UpdateUserRoleRequest } from './user-management.models';

@Injectable({ providedIn: 'root' })
export class UserManagementService {
  private readonly endpoint = `${environment.apiUrl}/users`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<ManagedUser[]> { return this.http.get<ManagedUser[]>(this.endpoint); }
  create(request: CreateUserRequest): Observable<ManagedUser> { return this.http.post<ManagedUser>(this.endpoint, request); }
  updateRole(id: number, request: UpdateUserRoleRequest): Observable<ManagedUser> {
    return this.http.put<ManagedUser>(`${this.endpoint}/${id}/role`, request);
  }
  remove(id: number): Observable<void> { return this.http.delete<void>(`${this.endpoint}/${id}`); }
}
