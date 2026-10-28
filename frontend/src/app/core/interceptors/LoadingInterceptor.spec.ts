import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AttendanceService } from '../../services/attendance.service';
import { LoadingService } from '../../services/LoadingService';
import { LoadingInterceptor } from './LoadingInterceptor';
import { localLoadingContext } from './loading-context';

describe('LoadingInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let attendance: AttendanceService;
  let loadingStates: boolean[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([LoadingInterceptor])), provideHttpClientTesting()]
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    attendance = TestBed.inject(AttendanceService);
    loadingStates = [];
    TestBed.inject(LoadingService).loading$.subscribe(value => loadingStates.push(value));
  });

  afterEach(() => controller.verify({ ignoreCancelled: true }));

  it('keeps global loading for a critical request until all concurrent global requests finish', () => {
    http.post('/auth/login', {}).subscribe();
    http.get('/users/me').subscribe();
    expect(loadingStates.at(-1)).toBeTrue();
    controller.expectOne('/auth/login').flush({ token: 'test-only' });
    expect(loadingStates.at(-1)).toBeTrue();
    controller.expectOne('/users/me').flush({});
    expect(loadingStates.at(-1)).toBeFalse();
  });

  it('does not show the global overlay for attendance writes or local reads', () => {
    attendance.createAttendance({ memberId: 1, eventId: 2, status: 'PRESENTE' }, localLoadingContext()).subscribe();
    http.get('/local', { context: localLoadingContext() }).subscribe();
    expect(loadingStates).toEqual([false]);
    controller.expectOne(request => request.url.endsWith('/attendances') && request.method === 'POST')
      .flush({ id: 3, memberId: 1, eventId: 2, status: 'PRESENTE' });
    controller.expectOne('/local').flush({});
    expect(loadingStates).toEqual([false]);
  });

  it('keeps the global default for attendance reads used outside the attendance route', () => {
    attendance.getAttendances().subscribe();
    expect(loadingStates.at(-1)).toBeTrue();
    controller.expectOne(request => request.url.endsWith('/attendances') && request.method === 'GET').flush([]);
    expect(loadingStates.at(-1)).toBeFalse();
  });

  it('releases global loading after error and cancellation', () => {
    http.get('/fails').subscribe({ error: () => {} });
    expect(loadingStates.at(-1)).toBeTrue();
    controller.expectOne('/fails').flush({}, { status: 500, statusText: 'Server Error' });
    expect(loadingStates.at(-1)).toBeFalse();

    const subscription = http.get('/cancel').subscribe();
    const cancelledRequest = controller.expectOne('/cancel');
    expect(loadingStates.at(-1)).toBeTrue();
    subscription.unsubscribe();
    expect(cancelledRequest.cancelled).toBeTrue();
    expect(loadingStates.at(-1)).toBeFalse();
  });
});
