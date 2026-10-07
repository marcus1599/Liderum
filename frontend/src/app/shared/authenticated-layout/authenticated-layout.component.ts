import { Component } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NavbarComponent } from '../navbar/navbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-authenticated-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MatSidenavModule, NavbarComponent, SidebarComponent],
  template: `
    <app-navbar [isHandset]="isHandset" (menuToggle)="sidenav.toggle()"></app-navbar>
    <mat-sidenav-container class="layout-container">
      <mat-sidenav #sidenav [mode]="isHandset ? 'over' : 'side'" [opened]="!isHandset"
                   [fixedInViewport]="isHandset" fixedTopGap="64">
        <app-sidebar [isHandset]="isHandset" (logoutClick)="logout()" (closeSidenav)="sidenav.close()"></app-sidebar>
      </mat-sidenav>
      <mat-sidenav-content class="layout-content"><router-outlet></router-outlet></mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .layout-container { height: calc(100vh - 64px); }
    .layout-content { min-height: 100%; background: #2e3139; }
  `]
})
export class AuthenticatedLayoutComponent {
  isHandset = false;

  constructor(private readonly breakpointObserver: BreakpointObserver, private readonly auth: AuthService) {
    this.breakpointObserver.observe([Breakpoints.Handset, Breakpoints.Tablet])
      .subscribe(result => this.isHandset = result.matches);
  }

  logout(): void { this.auth.logout(); }
}
