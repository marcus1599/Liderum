import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { MemberService } from './member.service';


describe('MemberService', () => {
  let service: MemberService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MemberService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('loads members through the configured endpoint', () => {
    service.getMembers().subscribe(members => expect(members).toEqual([]));

    const request = httpTestingController.expectOne(request => request.url.endsWith('/members'));
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
