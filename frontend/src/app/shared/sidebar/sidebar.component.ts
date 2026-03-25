import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
  imports: [
    MatIconModule,
    CommonModule
  ]
})
export class SidebarComponent {
  @Input() isHandset = false;

  @Output() showMembersChange = new EventEmitter<boolean>();
  @Output() logoutClick = new EventEmitter<void>();
  @Output() showEventsChange = new EventEmitter<boolean>();
  @Output() showAttendenceChange = new EventEmitter<boolean>();
  @Output() showSettingsChange = new EventEmitter<boolean>();
  @Output() showDashboardChange = new EventEmitter<boolean>();
  @Output() showGroupsChange = new EventEmitter<boolean>();
  @Output() closeSidenav = new EventEmitter<void>();

  private closeIfHandset() {
    if (this.isHandset) {
      this.closeSidenav.emit();
    }
  }

  setShowMembers(value: boolean) {
    this.showMembersChange.emit(value);
    this.closeIfHandset();
  }

  logout() {
    this.logoutClick.emit();
    this.closeIfHandset();
  }

  setShowEvents(value: boolean) {
    this.showEventsChange.emit(value);
    this.closeIfHandset();
  }

  setShowAttendence(value: boolean) {
    this.showAttendenceChange.emit(value);
    this.closeIfHandset();
  }

  setShowSettings(value: boolean) {
    this.showSettingsChange.emit(value);
    this.closeIfHandset();
  }

  setShowDashboard(value: boolean) {
    this.showDashboardChange.emit(value);
    this.closeIfHandset();
  }

  setShowGroups(value: boolean) {
    this.showGroupsChange.emit(value);
    this.closeIfHandset();
  }
}