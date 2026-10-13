import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../services/dashboard.service';
import { of } from 'rxjs';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [{
        provide: DashboardService,
        useValue: { getDashboardMetrics: () => of({ presencePie: [], presenceLine: [], classPie: [], highlights: [], alerts: [] }) }
      }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
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
