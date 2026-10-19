import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { GroupService } from './group.service';

describe('GroupService leader operations', () => {
  let service: GroupService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(GroupService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads reduced leader candidates from the team endpoint', () => {
    service.getLeaderCandidates().subscribe(candidates => expect(candidates).toEqual([{ id: 5, username: 'Active' }]));
    const request = http.expectOne(request => request.url.endsWith('/teams/leader-candidates'));
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 5, username: 'Active' }]);
  });

  it('sends only leaderId in the dedicated reassignment contract', () => {
    service.reassignLeader(8, 5).subscribe();
    const request = http.expectOne(request => request.url.endsWith('/teams/8/leader'));
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ leaderId: 5 });
    request.flush({ id: 8, name: 'Raid', leaderId: 5, leaderName: 'Active', members: [] });
  });
});
