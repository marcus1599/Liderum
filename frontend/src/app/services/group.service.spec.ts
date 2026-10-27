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

  it('loads member-only leadership candidates scoped to the selected team', () => {
    service.getLeaderCandidates(8).subscribe(candidates => expect(candidates).toEqual([{ id: 5, nickname: 'Unlinked member' }]));
    const request = http.expectOne(request => request.url.endsWith('/teams/8/leader-candidates'));
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 5, nickname: 'Unlinked member' }]);
  });

  it('sends only leaderMemberId in the dedicated reassignment contract', () => {
    service.reassignLeader(8, 5).subscribe();
    const request = http.expectOne(request => request.url.endsWith('/teams/8/leader'));
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ leaderMemberId: 5 });
    request.flush({ id: 8, name: 'Raid', leaderMemberId: 5, leaderNickname: 'Unlinked member', members: [] });
  });
});
