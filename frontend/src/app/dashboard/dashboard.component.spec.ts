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

  it('retries loading metrics after a failed request', () => {
    const pending = new Subject<any>();
    dashboardService.getDashboardMetrics.and.returnValue(pending);
    component.loadDashboardData();
    pending.error({ status: 500 });
    fixture.detectChanges();

    const retryButton: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(retryButton.textContent).toContain('Tentar carregar novamente');
    dashboardService.getDashboardMetrics.and.returnValue(of({
      presencePie: [], presenceLine: [], classPie: [], highlights: [], alerts: []
    }));
    retryButton.click();
    fixture.detectChanges();

    expect(dashboardService.getDashboardMetrics).toHaveBeenCalledTimes(3);
    expect(component.dashboardLoaded).toBeTrue();
    expect(component.errorMessage).toBe('');
    expect(fixture.nativeElement.querySelector('button')).toBeNull();
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

  it('explains when there are no events and avoids presenting zero observations as a measured rate', () => {
    const element: HTMLElement = fixture.nativeElement;
    expect(component.presenceRate).toBeNull();
    expect(element.querySelector('.kpi-value')?.textContent?.trim()).toBe('—');
    expect(element.textContent).toContain('Sem eventos nesta seleção');
    expect(element.textContent).toContain('Adicione membros para ver a distribuição por classe.');
    expect(element.textContent).toContain('Adicione membros para acompanhar alertas de presença.');
  });

  it('distinguishes events without attendance records from a measured zero rate', () => {
    dashboardService.getDashboardMetrics.and.returnValue(of({
      presencePie: [{ status: 'Presente', count: 0 }, { status: 'Faltou', count: 0 }, { status: 'Justificou', count: 0 }],
      presenceLine: [{ event: 'Sessão de teste', presentes: 0, faltas: 0, justificados: 0 }],
      classPie: [{ classe: 'Guerreiro', count: 2 }],
      highlights: [{ member: { nickname: 'Membro de teste' }, presencas: 0 }],
      alerts: []
    }));
    component.loadDashboardData();
    fixture.detectChanges();

    expect(component.hasEvents).toBeTrue();
    expect(component.hasMembers).toBeTrue();
    expect(component.presenceRate).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Há eventos nesta seleção, mas ainda não há registros de presença.');
    expect(fixture.nativeElement.textContent).toContain('Dados insuficientes para avaliar faltas consecutivas nesta seleção.');
    expect(fixture.nativeElement.textContent).toContain('Membro de teste');
    expect(fixture.nativeElement.textContent).toContain('Guerreiro');
  });

  it('keeps populated panels, zero-valued rankings, and filter behavior intact', () => {
    dashboardService.getDashboardMetrics.and.returnValue(of({
      presencePie: [{ status: 'Presente', count: 2 }, { status: 'Faltou', count: 1 }, { status: 'Justificou', count: 0 }],
      presenceLine: [{ event: 'Sessão de teste', presentes: 2, faltas: 1, justificados: 0 }],
      classPie: [{ classe: 'Guerreiro', count: 2 }],
      highlights: [{ member: { nickname: 'Membro de teste' }, presencas: 0 }],
      alerts: []
    }));
    component.loadDashboardData();
    fixture.detectChanges();

    expect(component.presenceRate).toBeCloseTo(66.666, 2);
    expect(component.hasPresenceRecords).toBeTrue();
    expect(fixture.nativeElement.textContent).toContain('Nenhum alerta calculado para os eventos selecionados.');
    expect(fixture.nativeElement.textContent).toContain('Membro de teste');
    expect(fixture.nativeElement.textContent).toContain('2');
    expect(dashboardService.getDashboardMetrics).toHaveBeenCalledWith(10);
  });
});
