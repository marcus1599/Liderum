import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { CommonModule } from '@angular/common';
import { AuthService } from '../auth/auth.service';
import { MembersComponent } from '../members/members.component';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { SidebarComponent } from '../shared/sidebar/sidebar.component';
import { EventsComponent } from '../events/events.component';

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
    EventsComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  showMembers = false;
  showEvents = false;
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
}
