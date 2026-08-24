import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { AuthInterceptor } from './auth.interceptor';

describe('AuthInterceptor', () => {
  let client: HttpClient;
  let http: HttpTestingController;
  let auth: { getToken: () => string | null; expireSession: jasmine.Spy };

  beforeEach(() => {
    auth = { getToken: () => 'jwt', expireSession: jasmine.createSpy('expireSession') };
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: { url: '/dashboard', navigate: jasmine.createSpy('navigate') } },
        provideHttpClient(withInterceptors([AuthInterceptor])),
        provideHttpClientTesting()
      ]
    });
    client = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('adds the Bearer token', () => {
    client.get('/users/me').subscribe();
    const request = http.expectOne('/users/me');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt');
    request.flush({});
  });

  it('does not redirect or clear the session for 403 and 404', () => {
    client.get('/users/me').subscribe({ error: () => undefined });
    http.expectOne('/users/me').flush({}, { status: 403, statusText: 'Forbidden' });
    client.get('/missing').subscribe({ error: () => undefined });
    http.expectOne('/missing').flush({}, { status: 404, statusText: 'Not Found' });
    expect(auth.expireSession).not.toHaveBeenCalled();
  });

  it('expires the session for an authenticated 401 but not login 401', () => {
    client.get('/users/me').subscribe({ error: () => undefined });
    http.expectOne('/users/me').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.expireSession).toHaveBeenCalledTimes(1);
    client.post('/auth/login', {}).subscribe({ error: () => undefined });
    http.expectOne('/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.expireSession).toHaveBeenCalledTimes(1);
  });
});
