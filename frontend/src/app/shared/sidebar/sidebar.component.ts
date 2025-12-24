import { Component, EventEmitter, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
  imports: [
    MatIconModule
  ]
})
export class SidebarComponent {
  @Output() showMembersChange = new EventEmitter<boolean>();
  @Output() logoutClick = new EventEmitter<void>();
  @Output() showEventsChange = new EventEmitter<boolean>();
  @Output() showAttendenceChange = new EventEmitter<boolean>();
  @Output() showSettingsChange = new EventEmitter<boolean>();
  @Output() showDashboardChange = new EventEmitter<boolean>();
  @Output() showGroupsChange = new EventEmitter<boolean>();

  setShowMembers(value: boolean) {
    this.showMembersChange.emit(value);
  }

  logout() {
    this.logoutClick.emit();
  }

  setShowEvents(value: boolean) {
    this.showEventsChange.emit(value);
  }

  setShowAttendence(value: boolean) {
    this.showAttendenceChange.emit(value);
  }
  setShowSettings(value: boolean) {
    this.showSettingsChange.emit(value);
  }
  setShowDashboard(value: boolean) {
    this.showDashboardChange.emit(value);
  }
  setShowGroups(value: boolean) {
    this.showGroupsChange.emit(value);
  }
}
