import { Component, OnInit, OnDestroy } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subject } from 'rxjs';
import { map, shareReplay, takeUntil } from 'rxjs/operators';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { NgChartsModule } from 'ng2-charts';

import { DashboardService, DashboardMetrics } from '../services/dashboard.service';

import { CommonModule } from '@angular/common';
import { AuthService } from '../auth/auth.service';
import { MembersComponent } from '../members/members.component';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { SidebarComponent } from '../shared/sidebar/sidebar.component';
import { EventsComponent } from '../events/events.component';
import { AttendenceComponent } from '../attendence/attendence.component';
import { SettingsComponent } from '../settings/settings.component';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { GroupsComponent } from '../groups/groups.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MembersComponent,
    NavbarComponent,
    SidebarComponent,
    EventsComponent,
    AttendenceComponent,
    SettingsComponent,
    MatCardModule,
    FormsModule,
    NgChartsModule,
    GroupsComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Valor síncrono — sem async pipe no template
  isHandset = false;

  showMembers = false;
  showEvents = false;
  showAttendence = false;
  showSettings = false;
  showDashboard = true;
  showGroups = false;
  eventFilterCount = 10;

  presencePieChartData = {
    labels: ['Presenças', 'Faltas', 'Justificadas'],
    datasets: [
      { data: [0, 0, 0], backgroundColor: ['#43a047', '#e53935', '#1e88e5'] }
    ]
  };

  presenceLineChartData = {
    labels: [] as string[],
    datasets: [
      { data: [] as number[], label: 'Presentes', borderColor: '#43a047', backgroundColor: 'rgba(67,160,71,0.2)' },
      { data: [] as number[], label: 'Faltas', borderColor: '#e53935', backgroundColor: 'rgba(229,57,53,0.2)' },
      { data: [] as number[], label: 'Justificados', borderColor: '#fbc02d', backgroundColor: 'rgba(251,192,45,0.2)' }
    ]
  };

  classPieChartData = {
    labels: [] as string[],
    datasets: [
      {
        data: [] as number[],
        backgroundColor: [
          '#8e24aa', '#1976d2', '#fbc02d', '#43a047', '#e53935',
          '#f4511e', '#6d4c41', '#546e7a', '#d81b60', '#00897b',
          '#c0ca33', '#5e35b1', '#039be5'
        ]
      }
    ]
  };

  highlights: { member: any, presencas: number }[] = [];
  alerts: { member: any, faltasConsecutivas: number }[] = [];

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private breakpointObserver: BreakpointObserver
  ) {}

  ngOnInit() {
    // Subscreve o breakpoint e atualiza a propriedade síncrona
    this.breakpointObserver
      .observe([Breakpoints.Handset, Breakpoints.Tablet])
      .pipe(
        map(result => result.matches),
        takeUntil(this.destroy$)
      )
      .subscribe(isHandset => {
        this.isHandset = isHandset;
      });

    this.loadDashboardData();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDashboardData() {
    this.dashboardService.getDashboardMetrics(this.eventFilterCount).subscribe((metrics: DashboardMetrics) => {
      this.presencePieChartData = {
        labels: metrics.presencePie.map(p => p.status),
        datasets: [{
          data: metrics.presencePie.map(p => p.count),
          backgroundColor: ['#43a047', '#e53935', '#1e88e5']
        }]
      };

      this.presenceLineChartData = {
        labels: metrics.presenceLine.map(e => e.event),
        datasets: [
          {
            label: 'Presentes',
            data: metrics.presenceLine.map(e => e.presentes),
            borderColor: '#43a047',
            backgroundColor: 'rgba(67,160,71,0.2)'
          },
          {
            label: 'Faltas',
            data: metrics.presenceLine.map(e => e.faltas),
            borderColor: '#e53935',
            backgroundColor: 'rgba(229,57,53,0.2)'
          },
          {
            label: 'Justificados',
            data: metrics.presenceLine.map(e => e.justificados),
            borderColor: '#fbc02d',
            backgroundColor: 'rgba(251,192,45,0.2)'
          }
        ]
      };

      this.classPieChartData = {
        labels: metrics.classPie.map(c => c.classe),
        datasets: [{
          data: metrics.classPie.map(c => c.count),
          backgroundColor: [
            '#8e24aa', '#1976d2', '#fbc02d', '#43a047', '#e53935',
            '#f4511e', '#6d4c41', '#546e7a', '#d81b60', '#00897b',
            '#c0ca33', '#5e35b1', '#039be5'
          ]
        }]
      };

      this.alerts = metrics.alerts;
      this.highlights = metrics.highlights;
    });
  }

  onEventFilterChange() {
    this.loadDashboardData();
  }

  activateView(view: string) {
    this.showDashboard = view === 'dashboard';
    this.showMembers = view === 'members';
    this.showGroups = view === 'groups';
    this.showEvents = view === 'events';
    this.showAttendence = view === 'attendence';
    this.showSettings = view === 'settings';
  }

  logout() {
    this.authService.logout();
  }
}