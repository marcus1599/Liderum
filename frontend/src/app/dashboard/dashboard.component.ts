import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { NgChartsModule } from 'ng2-charts';

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
    NgChartsModule
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

  // Gráfico de pizza - Distribuição por Classe (Chart.js v4+)
  classPieChartData = {
    labels: ['Guerreiros', 'Magos', 'Sacerdotes', 'Arqueiros'],
    datasets: [
      { data: [12, 8, 5, 5], backgroundColor: ['#8e24aa', '#1976d2', '#fbc02d', '#43a047'] }
    ]
  };

  constructor(private authService: AuthService) {}

  newEvent() {
    // Logic to create a new event
    console.log('New event created');
  }
  newMember() {
    // Logic to add a new member
    console.log('New member added');
  }

  logout() {
    this.authService.logout();
  }

  activateView(view: 'dashboard' | 'members' | 'events' | 'attendence' | 'settings') {
    this.showDashboard = view === 'dashboard';
    this.showMembers = view === 'members';
    this.showEvents = view === 'events';
    this.showAttendence = view === 'attendence';
    this.showSettings = view === 'settings';
  }
}
