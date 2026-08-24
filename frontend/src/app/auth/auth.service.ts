import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of, switchMap, tap, catchError, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, UserProfile } from './auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = environment.apiUrl + '/auth';
  private profileSubject = new BehaviorSubject<UserProfile | null>(null);
  private restored = false;
  readonly profile$ = this.profileSubject.asObservable();

  constructor(private http: HttpClient,private router:Router) {}

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap(res => localStorage.setItem('token', res.token)),
      switchMap(res => this.loadCurrentUser().pipe(map(() => res))),
      catchError(error => {
        if (error.status !== 401) {
          this.clearState();
        }
        throw error;
      })
    );
  }

  loadCurrentUser(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiUrl}/users/me`).pipe(
      tap(profile => {
        this.profileSubject.next(profile);
        this.restored = true;
      })
    );
  }

  restoreSession(): Observable<boolean> {
    const token = this.getToken();
    if (!token) {
      this.restored = true;
      this.profileSubject.next(null);
      return of(false);
    }
    if (this.restored && this.profileSubject.value) {
      return of(true);
    }
    return this.loadCurrentUser().pipe(
      map(() => true),
      catchError(() => {
        this.clearState();
        return of(false);
      })
    );
  }

  logout() {
    this.clearState();
    this.router.navigate(['/login']);
  }

  expireSession() {
    this.clearState();
    if (!this.router.url.startsWith('/login')) {
      this.router.navigate(['/login']);
    }
  }

  private clearState() {
    localStorage.removeItem('token');
    this.profileSubject.next(null);
    this.restored = false;
  }
  

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
