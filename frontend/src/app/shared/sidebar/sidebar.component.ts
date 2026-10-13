import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/auth.service';
import { Observable } from 'rxjs';
import { UserProfile } from '../../auth/auth.models';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
  imports: [
    MatIconModule,
    CommonModule,
    RouterLink,
    RouterLinkActive
  ],
  standalone: true
})
export class SidebarComponent {
  @Input() isHandset = false;

  @Output() logoutClick = new EventEmitter<void>();
  @Output() closeSidenav = new EventEmitter<void>();

  readonly profile$: Observable<UserProfile | null>;

  constructor(auth: AuthService) {
    this.profile$ = auth.profile$;
  }

  closeIfHandset(): void {
    if (this.isHandset) {
      this.closeSidenav.emit();
    }
  }

  logout() {
    this.logoutClick.emit();
    this.closeIfHandset();
  }
}
