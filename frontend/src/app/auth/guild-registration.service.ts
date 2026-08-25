import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { GuildRegistrationRequest, GuildRegistrationResponse } from './guild-registration.models';

@Injectable({ providedIn: 'root' })
export class GuildRegistrationService {
  private readonly endpoint = `${environment.apiUrl}/auth/register-guild`;

  constructor(private readonly http: HttpClient) {}

  register(request: GuildRegistrationRequest): Observable<GuildRegistrationResponse> {
    return this.http.post<GuildRegistrationResponse>(this.endpoint, request);
  }
}
