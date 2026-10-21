import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../services/dashboard.service';
import { of, Subject } from 'rxjs';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let dashboardService: { getDashboardMetrics: jasmine.Spy };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [{ provide: DashboardService, useValue: dashboardService = { getDashboardMetrics: jasmine.createSpy('getDashboardMetrics').and.returnValue(of({ presencePie: [], presenceLine: [], classPie: [], highlights: [], alerts: [] })) } }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not show dashboard content while metrics are loading or failed', () => {
    const pending = new Subject<any>();
    dashboardService.getDashboardMetrics.and.returnValue(pending);
    component.loadDashboardData();
    fixture.detectChanges();
    expect(component.loading).toBeTrue();
    expect(fixture.nativeElement.querySelector('.dashboard-grid')).toBeNull();
    pending.error({ status: 500 });
    fixture.detectChanges();
    expect(component.dashboardLoaded).toBeFalse();
    expect(fixture.nativeElement.textContent).toContain('Não foi possível carregar');
  });

  it('clamps the event filter before requesting metrics', () => {
    component.eventFilterCount = 999;
    component.loadDashboardData();
    expect(dashboardService.getDashboardMetrics).toHaveBeenCalledWith(50);
  });

  it('renders only dashboard content and does not embed domain screens or a second navigation shell', () => {
    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('mat-sidenav-container')).toBeNull();
    expect(element.querySelector('app-navbar')).toBeNull();
    expect(element.querySelector('app-sidebar')).toBeNull();
    expect(element.querySelector('app-members, app-groups, app-events, app-attendence, app-settings')).toBeNull();
    expect(element.querySelector('.dashboard-grid')).not.toBeNull();
  });
});
