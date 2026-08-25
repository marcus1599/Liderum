import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserManagementService } from './user-management.service';

describe('UserManagementService', () => {
  let service: UserManagementService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [UserManagementService, provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(UserManagementService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('uses explicit tenant-scoped user contracts without guildId', () => {
    service.list().subscribe();
    let req = http.expectOne(r => r.url.endsWith('/users')); expect(req.request.method).toBe('GET'); req.flush([]);
    service.create({ username: 'soldier', email: 's@example.com', password: 'password123', role: 'SOLDADO' }).subscribe();
    req = http.expectOne(r => r.url.endsWith('/users')); expect(req.request.body).toEqual({ username: 'soldier', email: 's@example.com', password: 'password123', role: 'SOLDADO' }); expect(req.request.body.guildId).toBeUndefined(); req.flush({});
    service.updateRole(2, { role: 'MAJOR' }).subscribe();
    req = http.expectOne(r => r.url.endsWith('/users/2/role')); expect(req.request.body).toEqual({ role: 'MAJOR' }); req.flush({});
    service.remove(2).subscribe(); req = http.expectOne(r => r.url.endsWith('/users/2')); expect(req.request.method).toBe('DELETE'); req.flush(null);
  });
});
