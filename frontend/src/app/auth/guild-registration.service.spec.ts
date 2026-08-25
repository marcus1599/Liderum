import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { GuildRegistrationService } from './guild-registration.service';

describe('GuildRegistrationService', () => {
  let service: GuildRegistrationService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [GuildRegistrationService, provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(GuildRegistrationService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('posts the typed registration contract without frontend-only fields', () => {
    const request = { guildName: 'Guild', serverName: 'Server', username: 'owner', email: 'owner@example.com', password: 'password123' };
    service.register(request).subscribe(response => expect(response.guildRole).toBe('MARECHAL'));
    const req = http.expectOne((r) => r.url.endsWith('/auth/register-guild'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    expect(req.request.body.confirmPassword).toBeUndefined();
    expect(req.request.body.guildId).toBeUndefined();
    expect(req.request.body.role).toBeUndefined();
    req.flush({ id: 1, username: 'owner', email: 'owner@example.com', guildRole: 'MARECHAL' });
  });

  it('propagates HTTP errors', () => {
    let status = 0;
    service.register({ guildName: 'G', serverName: 'S', username: 'owner', email: 'owner@example.com', password: 'password123' })
      .subscribe({ error: error => status = error.status });
    http.expectOne((r) => r.url.endsWith('/auth/register-guild')).flush({}, { status: 400, statusText: 'Bad Request' });
    expect(status).toBe(400);
  });
});
