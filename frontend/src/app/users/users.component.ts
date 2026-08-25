import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';
import { CreateUserRequest, GuildRole, ManagedUser } from './user-management.models';
import { UserManagementService } from './user-management.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSnackBarModule, MatProgressSpinner],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  users: ManagedUser[] = [];
  profile: UserProfile | null = null;
  isLoading = true;
  isSaving = false;
  errorMessage = '';
  readonly form: FormGroup;
  readonly roleChanges: Record<number, GuildRole> = {};
  private profileSubscription?: Subscription;

  constructor(
    private readonly fb: FormBuilder,
    private readonly usersService: UserManagementService,
    private readonly authService: AuthService,
    private readonly snackbar: MatSnackBar,
    private readonly router: Router
  ) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(128)]],
      role: ['SOLDADO' as GuildRole, Validators.required]
    });
  }

  ngOnInit(): void {
    this.profileSubscription = this.authService.profile$.subscribe(profile => {
      this.profile = profile;
      if (!this.canManageUsers()) {
        this.isLoading = false;
        return;
      }
      this.loadUsers();
    });
  }

  ngOnDestroy(): void { this.profileSubscription?.unsubscribe(); }

  canManageUsers(): boolean { return this.profile?.guildRole === 'MARECHAL' || this.profile?.guildRole === 'GENERAL'; }
  isMarechal(): boolean { return this.profile?.guildRole === 'MARECHAL'; }

  allowedCreateRoles(): GuildRole[] {
    return this.isMarechal() ? ['MARECHAL', 'GENERAL', 'MAJOR', 'CAPITÃO', 'SOLDADO'] : ['MAJOR', 'CAPITÃO', 'SOLDADO'];
  }

  canActOn(target: ManagedUser): boolean {
    if (this.isMarechal()) return target.id !== this.profile?.id;
    return target.guildRole !== 'GENERAL' && target.guildRole !== 'MARECHAL';
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.usersService.list().subscribe({
      next: users => { this.users = users; this.isLoading = false; },
      error: error => { this.isLoading = false; this.errorMessage = this.safeError(error); }
    });
  }

  createUser(): void {
    if (this.form.invalid || this.isSaving) return;
    this.isSaving = true;
    const request = this.form.getRawValue() as CreateUserRequest;
    this.usersService.create(request).subscribe({
      next: user => {
        this.users = [...this.users, user];
        this.form.reset({ role: 'SOLDADO' });
        this.isSaving = false;
        this.snackbar.open('Usuário criado com sucesso.', 'Fechar');
      },
      error: error => { this.isSaving = false; this.snackbar.open(this.safeError(error), 'Fechar'); }
    });
  }

  updateRole(user: ManagedUser): void {
    if (!this.canActOn(user)) return;
    const role = this.roleChanges[user.id] || user.guildRole;
    if (role === user.guildRole) return;
    this.usersService.updateRole(user.id, { role }).subscribe({
      next: updated => {
        this.users = this.users.map(item => item.id === updated.id ? updated : item);
        this.snackbar.open('Role atualizada com sucesso.', 'Fechar');
      },
      error: error => this.snackbar.open(this.safeError(error), 'Fechar')
    });
  }

  removeUser(user: ManagedUser): void {
    if (!this.canActOn(user) || !window.confirm(`Remover ${user.username}?`)) return;
    this.usersService.remove(user.id).subscribe({
      next: () => {
        this.users = this.users.filter(item => item.id !== user.id);
        this.snackbar.open('Usuário removido com sucesso.', 'Fechar');
      },
      error: error => this.snackbar.open(this.safeError(error), 'Fechar')
    });
  }

  private safeError(error: HttpErrorResponse): string {
    if (error.status === 401) return 'Sua sessão expirou. Faça login novamente.';
    if (error.status === 403) return 'Você não tem permissão para esta ação.';
    if (error.status === 404) return 'Usuário não encontrado nesta Guild.';
    if (error.status >= 400 && error.status < 500) return 'Não foi possível concluir a operação. Confira os dados.';
    return 'Não foi possível concluir a operação agora.';
  }
}
