import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent {
  darkTheme = false;

  toggleTheme(): void {
    this.darkTheme = !this.darkTheme;
    const themeClass = this.darkTheme ? 'dark-theme' : 'light-theme';
    document.body.className = themeClass;
  }

  logout() {
    localStorage.removeItem('token');
    location.href = '/auth'; // ou use Router se quiser redirecionar com mais controle
  }
}
