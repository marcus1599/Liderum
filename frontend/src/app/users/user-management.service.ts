import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CreateUserRequest, ManagedUser, UpdateUserRoleRequest, UserActivationResponse } from './user-management.models';

@Injectable({ providedIn: 'root' })
export class UserManagementService {
  private readonly endpoint = `${environment.apiUrl}/users`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<ManagedUser[]> { return this.http.get<ManagedUser[]>(this.endpoint); }
  create(request: CreateUserRequest): Observable<UserActivationResponse> { return this.http.post<UserActivationResponse>(this.endpoint, request); }
  activate(token: string, password: string): Observable<void> { return this.http.post<void>(`${environment.apiUrl}/auth/activate`, { token, password }); }
  regenerateActivation(id: number): Observable<UserActivationResponse> { return this.http.post<UserActivationResponse>(`${this.endpoint}/${id}/activation`, {}); }
  updateRole(id: number, request: UpdateUserRoleRequest): Observable<ManagedUser> {
    return this.http.put<ManagedUser>(`${this.endpoint}/${id}/role`, request);
  }
  remove(id: number): Observable<void> { return this.http.delete<void>(`${this.endpoint}/${id}`); }
}
