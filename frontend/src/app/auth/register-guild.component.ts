import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { GuildRegistrationService } from './guild-registration.service';

function matchingPasswords(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmation = control.get('confirmPassword')?.value;
  return password && confirmation && password !== confirmation ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-register-guild',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinner,
    MatSnackBarModule,
    RouterLink
  ],
  templateUrl: './register-guild.component.html',
  styleUrls: ['./register-guild.component.scss']
})
export class RegisterGuildComponent {
  readonly form: FormGroup;

  isLoading = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly registration: GuildRegistrationService,
    private readonly snackbar: MatSnackBar,
    private readonly router: Router
  ) {
    this.form = this.fb.group({
      guildName: ['', [Validators.required, Validators.maxLength(120)]],
      serverName: ['', [Validators.required, Validators.maxLength(120)]],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(128)]],
      confirmPassword: ['', Validators.required]
    }, { validators: matchingPasswords });
  }

  submit(): void {
    if (this.form.invalid || this.isLoading) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { guildName, serverName, username, email, password } = this.form.getRawValue();
    this.registration.register({ guildName: guildName!, serverName: serverName!, username: username!, email: email!, password: password! }).subscribe({
      next: () => {
        this.isLoading = false;
        this.form.reset();
        this.snackbar.open('Guild criada com sucesso. Faça login para continuar.', 'Fechar');
        this.router.navigate(['/login']);
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.snackbar.open(this.errorMessage(error), 'Fechar');
      }
    });
  }

  private errorMessage(error: HttpErrorResponse): string {
    if (error.status === 400 || (error.status >= 400 && error.status < 500)) {
      return 'Não foi possível criar a Guild. Confira os dados informados.';
    }
    return 'Não foi possível criar a Guild agora. Tente novamente mais tarde.';
  }
}
