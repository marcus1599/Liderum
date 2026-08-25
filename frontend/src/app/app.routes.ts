import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './auth/login.component';
import { MembersComponent } from './members/members.component';
import { authGuard } from './auth/auth.guard';
import { RegisterGuildComponent } from './auth/register-guild.component';
import { UsersComponent } from './users/users.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterGuildComponent },
  { path: 'users', component: UsersComponent, canActivate: [authGuard] },
  { path: 'members', component: MembersComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' },
];
