import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './auth/login.component';
import { MembersComponent } from './members/members.component';
import { authGuard } from './auth/auth.guard';
import { RegisterGuildComponent } from './auth/register-guild.component';
import { UsersComponent } from './users/users.component';
import { GroupsComponent } from './groups/groups.component';
import { EventsComponent } from './events/events.component';
import { AttendenceComponent } from './attendence/attendence.component';
import { ActivateComponent } from './auth/activate.component';
import { AuthenticatedLayoutComponent } from './shared/authenticated-layout/authenticated-layout.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterGuildComponent },
  { path: 'activate', component: ActivateComponent },
  {
    path: '', component: AuthenticatedLayoutComponent, canActivate: [authGuard], children: [
      { path: 'users', component: UsersComponent },
      { path: 'members', component: MembersComponent },
      { path: 'teams', component: GroupsComponent },
      { path: 'events', component: EventsComponent },
      { path: 'attendance', component: AttendenceComponent }
    ]
  },
  { path: '**', redirectTo: 'login' },
];
