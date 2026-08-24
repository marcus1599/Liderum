import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthResponse, UserProfile } from './auth.models';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate'), url: '/login' } },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('persists only the JWT and loads the authenticated profile after login', () => {
    let result: AuthResponse | undefined;
    service.login({ username: 'marechal', password: 'secret' }).subscribe(value => result = value);
    http.expectOne(request => request.url.endsWith('/auth/login')).flush({ token: 'jwt', role: 'MARECHAL' });
    http.expectOne(request => request.url.endsWith('/users/me')).flush({ id: 1, username: 'marechal', email: 'm@example.com', guildRole: 'MARECHAL' } satisfies UserProfile);
    expect(result?.token).toBe('jwt');
    expect(localStorage.getItem('token')).toBe('jwt');
    expect(localStorage.getItem('role')).toBeNull();
    expect(localStorage.getItem('guildId')).toBeNull();
  });

  it('restores a token through /users/me and rejects an invalid token', () => {
    localStorage.setItem('token', 'stale');
    let restored: boolean | undefined;
    service.restoreSession().subscribe(value => restored = value);
    http.expectOne(request => request.url.endsWith('/users/me')).flush({ status: 401 }, { status: 401, statusText: 'Unauthorized' });
    expect(restored).toBeFalse();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('does not call the API when no token exists and logout clears memory', () => {
    let restored: boolean | undefined;
    service.restoreSession().subscribe(value => restored = value);
    expect(restored).toBeFalse();
    service.logout();
    expect(localStorage.getItem('token')).toBeNull();
  });
});
