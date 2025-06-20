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

  setShowMembers(value: boolean) {
    this.showMembersChange.emit(value);
  }

  logout() {
    this.logoutClick.emit();
  }
}
