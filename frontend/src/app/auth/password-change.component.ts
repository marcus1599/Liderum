import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnDestroy } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { AuthService } from './auth.service';

function matchingPassword(control: AbstractControl): ValidationErrors | null {
  return control.get('newPassword')?.value === control.get('confirmation')?.value
    ? null : { passwordMismatch: true };
}

function bcryptByteLimit(control: AbstractControl): ValidationErrors | null {
  const password = control.value as string | null;
  return password && new TextEncoder().encode(password).length > 72 ? { bcryptByteLimit: true } : null;
}

@Component({
  selector: 'app-password-change',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatProgressSpinnerModule, MatSnackBarModule],
  templateUrl: './password-change.component.html',
  styleUrl: './password-change.component.scss'
})
export class PasswordChangeComponent implements OnDestroy {
  private readonly formBuilder = inject(FormBuilder);

  readonly form = this.formBuilder.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(128), bcryptByteLimit]],
    confirmation: ['', Validators.required]
  }, { validators: matchingPassword });

  saving = false;
  errorMessage = '';
  requiresReauthentication = false;

  constructor(
    private readonly auth: AuthService,
    private readonly snack: MatSnackBar
  ) {}

  submit(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    const { currentPassword, newPassword } = this.form.getRawValue();
    if (!currentPassword || !newPassword) return;

    this.saving = true;
    this.errorMessage = '';
    this.auth.changeOwnPassword({ currentPassword, newPassword }).pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => {
          this.clearPasswords();
          this.snack.open('Senha alterada. Entre novamente com a nova senha.', 'Fechar');
          this.auth.logout();
        },
        error: (error: HttpErrorResponse) => this.handleError(error)
      });
  }

  goToLogin(): void {
    this.clearPasswords();
    this.auth.logout();
  }

  ngOnDestroy(): void {
    this.clearPasswords();
  }

  private handleError(error: HttpErrorResponse): void {
    const code = error.error?.message;
    if (error.status === 400 && code === 'CURRENT_PASSWORD_INVALID') {
      this.form.controls.currentPassword.reset('');
      this.errorMessage = 'A senha atual não confere. Tente novamente.';
      return;
    }
    if (error.status === 400 && code === 'PASSWORD_UNCHANGED') {
      this.form.controls.newPassword.reset('');
      this.form.controls.confirmation.reset('');
      this.errorMessage = 'Escolha uma senha diferente da atual.';
      return;
    }
    if (error.status === 400) {
      this.errorMessage = 'A nova senha não atende aos limites permitidos. Confira os campos.';
      return;
    }
    if (error.status === 401) {
      this.errorMessage = 'Sua sessão expirou. Entre novamente.';
      return;
    }
    if (error.status === 403) {
      this.errorMessage = 'Você não tem permissão para alterar a senha nesta sessão.';
      return;
    }
    this.requiresReauthentication = true;
    this.errorMessage = 'Não foi possível confirmar a alteração. Entre novamente para verificar a senha da conta.';
  }

  private clearPasswords(): void {
    this.form.reset({ currentPassword: '', newPassword: '', confirmation: '' });
  }
}
