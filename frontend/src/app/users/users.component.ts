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
  activationLink = '';
  activationCopyFailed = false;
  readonly form: FormGroup;
  readonly roleChanges: Record<number, GuildRole> = {};
  readonly statusChanges: Record<number, boolean> = {};
  readonly actionChanges: Record<number, boolean> = {};
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

  roleLabel(role: GuildRole): string { return role; }

  statusLabel(status: ManagedUser['status']): string {
    return { PENDING: 'Aguardando ativação', ACTIVE: 'Ativo', DISABLED: 'Desativado' }[status];
  }

  actionStatus(user: ManagedUser): string {
    if (this.statusChanges[user.id]) return `Atualizando o status de ${user.username}.`;
    if (this.actionChanges[user.id]) return `Atualizando ${user.username}.`;
    return '';
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
    this.clearActivationLink();
    const request = this.form.getRawValue() as CreateUserRequest;
    this.usersService.create(request).subscribe({
      next: response => {
        this.users = [...this.users, response.user];
        this.form.reset({ role: 'SOLDADO' });
        this.isSaving = false;
        this.prepareActivationLink(response.activationToken, 'Usuário criado.');
      },
      error: error => { this.isSaving = false; this.snackbar.open(this.safeError(error), 'Fechar'); }
    });
  }

  updateRole(user: ManagedUser): void {
    if (!this.canActOn(user) || this.actionChanges[user.id]) return;
    const role = this.roleChanges[user.id] || user.guildRole;
    if (role === user.guildRole) return;
    this.actionChanges[user.id] = true;
    this.usersService.updateRole(user.id, { role }).subscribe({
      next: updated => {
        this.users = this.users.map(item => item.id === updated.id ? updated : item);
        this.actionChanges[user.id] = false;
        this.snackbar.open('Cargo atualizado com sucesso.', 'Fechar');
      },
      error: error => {
        this.actionChanges[user.id] = false;
        this.snackbar.open(this.safeError(error), 'Fechar');
      }
    });
  }

  changeStatus(user: ManagedUser): void {
    if (!this.canActOn(user) || !['ACTIVE', 'DISABLED'].includes(user.status)) return;
    const nextStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
    const action = nextStatus === 'DISABLED' ? 'desativar' : 'reativar';
    if (!window.confirm(`Deseja ${action} ${user.username}?`)) return;
    this.statusChanges[user.id] = true;
    this.usersService.updateStatus(user.id, nextStatus).subscribe({
      next: updated => {
        this.users = this.users.map(item => item.id === updated.id ? updated : item);
        this.statusChanges[user.id] = false;
        this.snackbar.open(nextStatus === 'DISABLED' ? 'Usuário desativado.' : 'Usuário reativado. Será necessário fazer login novamente.', 'Fechar');
      },
      error: error => {
        this.statusChanges[user.id] = false;
        this.snackbar.open(error.status === 409 ? 'Não foi possível alterar o status. Verifique se o usuário lidera uma equipe ou se a Guild mantém um MARECHAL ativo.' : this.safeError(error), 'Fechar');
      }
    });
  }

  removeUser(user: ManagedUser): void {
    if (!this.canActOn(user) || this.actionChanges[user.id] || !window.confirm(`Remover ${user.username}?`)) return;
    this.actionChanges[user.id] = true;
    this.usersService.remove(user.id).subscribe({
      next: () => {
        this.users = this.users.filter(item => item.id !== user.id);
        this.snackbar.open('Usuário removido com sucesso.', 'Fechar');
      },
      error: error => {
        this.actionChanges[user.id] = false;
        this.snackbar.open(this.safeError(error), 'Fechar');
      }
    });
  }

  regenerateActivation(user: ManagedUser): void {
    if (!this.canActOn(user) || user.status !== 'PENDING' || this.actionChanges[user.id]) return;
    this.actionChanges[user.id] = true;
    this.clearActivationLink();
    this.usersService.regenerateActivation(user.id).subscribe({
      next: response => {
        this.actionChanges[user.id] = false;
        this.prepareActivationLink(response.activationToken, 'Novo link de ativação.');
      },
      error: error => {
        this.actionChanges[user.id] = false;
        this.snackbar.open(this.safeError(error), 'Fechar');
      }
    });
  }

  selectActivationLink(input: HTMLInputElement): void {
    input.focus();
    input.select();
  }

  private clearActivationLink(): void {
    this.activationLink = '';
    this.activationCopyFailed = false;
  }

  private prepareActivationLink(token: string, message: string): void {
    this.activationLink = `${window.location.origin}/activate?token=${encodeURIComponent(token)}`;
    this.activationCopyFailed = true;
    const clipboard = this.getClipboard();
    if (!clipboard) {
      this.snackbar.open(`${message} Selecione o link abaixo para copiá-lo manualmente.`, 'Fechar');
      return;
    }
    clipboard.writeText(this.activationLink).then(() => {
      this.activationCopyFailed = false;
      this.snackbar.open(`${message} Link de ativação copiado.`, 'Fechar');
    }).catch(() => {
      this.snackbar.open(`${message} Não foi possível copiar automaticamente. Selecione o link abaixo.`, 'Fechar');
    });
  }

  private getClipboard(): Pick<Clipboard, 'writeText'> | undefined {
    return navigator.clipboard;
  }

  private safeError(error: HttpErrorResponse): string {
    if (error.status === 401) return 'Sua sessão expirou. Faça login novamente.';
    if (error.status === 403) return 'Você não tem permissão para esta ação.';
    if (error.status === 404) return 'Usuário não encontrado nesta Guild.';
    if (error.status >= 400 && error.status < 500) return 'Não foi possível concluir a operação. Confira os dados.';
    return 'Não foi possível concluir a operação agora.';
  }

  fieldError(controlName: string): string {
    const control = this.form.get(controlName);
    if (!control || !control.touched || control.valid) return '';
    if (control.hasError('required')) return 'Campo obrigatório.';
    if (control.hasError('email')) return 'Informe um e-mail válido.';
    if (control.hasError('minlength')) return 'Informe pelo menos 3 caracteres.';
    return 'Valor inválido.';
  }
}
