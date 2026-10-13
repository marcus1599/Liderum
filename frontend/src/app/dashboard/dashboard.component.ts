import { Component, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { NgChartsModule } from 'ng2-charts';

import { DashboardService, DashboardMetrics } from '../services/dashboard.service';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { SumPipe } from './sum.pipe';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    FormsModule,
    NgChartsModule,
    SumPipe
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
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
    private dashboardService: DashboardService
  ) {}

  ngOnInit() {
    this.loadDashboardData();
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

}
