import { Component } from '@angular/core';
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
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    NgChartsModule,
    GroupsComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  showMembers = false;
  showEvents = false;
  showAttendence = false;
  showSettings = false;
  showDashboard = true;
  showGroups = false;
  eventFilterCount = 10;

  // Gráfico de pizza - Taxa de Presença Geral
  presencePieLabels: string[] = ['Presenças', 'Faltas', 'Justificadas'];
  presencePieData: number[] = [60, 30, 10]; // Exemplo mock
  presencePieColors = [{ backgroundColor: ['#43a047', '#e53935', '#1e88e5'] }];

  // Gráfico de linha - Evolução de Presença
  presenceLineLabels: string[] = ['Evento 1', 'Evento 2', 'Evento 3', 'Evento 4', 'Evento 5'];
  presenceLineData = [{ data: [18, 22, 20, 25, 19], label: 'Presentes' }];
  presenceLineColors = [{ borderColor: '#43a047', backgroundColor: 'rgba(67,160,71,0.1)' }];

  // Gráfico de pizza - Distribuição por Classe
  classPieLabels: string[] = ['Guerreiros', 'Magos', 'Sacerdotes', 'Arqueiros'];
  classPieData: number[] = [12, 8, 5, 5];
  classPieColors = [{ backgroundColor: ['#8e24aa', '#1976d2', '#fbc02d', '#43a047'] }];

  // Gráfico de pizza - Taxa de Presença Geral (Chart.js v4+)
  presencePieChartData = {
    labels: ['Presenças', 'Faltas', 'Justificadas'],
    datasets: [
      { data: [60, 30, 10], backgroundColor: ['#43a047', '#e53935', '#1e88e5'] }
    ]
  };

  // Gráfico de linha - Evolução de Presença (Chart.js v4+)
  presenceLineChartData = {
    labels: ['Evento 1', 'Evento 2', 'Evento 3', 'Evento 4', 'Evento 5'],
    datasets: [
      { data: [18, 22, 20, 25, 19], label: 'Presentes', borderColor: '#43a047', backgroundColor: 'rgba(67,160,71,0.1)' }
    ]
  };


  classPieChartData = {
    labels: ['Guerreiros', 'Magos', 'Sacerdotes', 'Arqueiros'],
    datasets: [
      { data: [12, 8, 5, 5], backgroundColor: ['#8e24aa', '#1976d2', '#fbc02d', '#43a047'] }
    ]
  };

  constructor(private authService: AuthService, private dashboardService: DashboardService) { }

  ngOnInit() {
    this.loadDashboardData();
  }
  loadDashboardData() {
    this.dashboardService.getDashboardMetrics(this.eventFilterCount).subscribe((metrics: DashboardMetrics) => {
      // Pie de presença
      this.presencePieChartData = {
        labels: metrics.presencePie.map(p => p.status),
        datasets: [{
          data: metrics.presencePie.map(p => p.count),
          backgroundColor: ['#43a047', '#e53935', '#1e88e5']
        }]
      };
      // Linha de presença
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
      // Pie de classes
      this.classPieChartData = {
        labels: metrics.classPie.map(c => c.classe),
        datasets: [{
          data: metrics.classPie.map(c => c.count),
          backgroundColor: ['#8e24aa', '#1976d2', '#fbc02d', '#43a047', '#e53935', '#f4511e', '#6d4c41', '#546e7a', '#d81b60', '#00897b', '#c0ca33', '#5e35b1', '#039be5', '#fb8c00', '#3949ab', '#00897b', '#d81b60', '#6d4c41']
        }]
      };
      // Alertas e destaques
      this.alerts = metrics.alerts;
      this.highlights = metrics.highlights;
    });
  }

  // Atualiza ao mudar o filtro de eventos
  onEventFilterChange() {
    this.loadDashboardData();
  }

  // Métodos para navegação entre views
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

  highlights: { member: any, presencas: number }[] = [];
  alerts: { member: any, faltasConsecutivas: number }[] = [];


}
