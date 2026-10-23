import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, of, Subject, throwError } from 'rxjs';
import { AuthResponse } from './auth.models';
import { AuthService } from './auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: { navigate: jasmine.Spy; createUrlTree: jasmine.Spy; serializeUrl: jasmine.Spy; events: Observable<never> };
  let snackBar: { open: jasmine.Spy };

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    authService.login.and.returnValue(of({ token: 'fixture-token', role: 'MARECHAL' }));
    router = {
      navigate: jasmine.createSpy('navigate'),
      createUrlTree: jasmine.createSpy('createUrlTree').and.returnValue({}),
      serializeUrl: jasmine.createSpy('serializeUrl').and.returnValue('/'),
      events: of() as Observable<never>
    };
    snackBar = { open: jasmine.createSpy('open') };

    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: {} },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    });
    TestBed.overrideProvider(MatSnackBar, { useValue: snackBar });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders labeled fields and keeps submit unavailable until required values exist', () => {
    expect(component).toBeTruthy();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(fixture.nativeElement.textContent).toContain('Usuário');
    expect(fixture.nativeElement.textContent).toContain('Senha');
    expect(button.disabled).toBeTrue();
  });

  it('navigates after the login service succeeds', () => {
    component.form.setValue({ username: 'fixture-user', password: 'x'.repeat(8) });
    fixture.detectChanges();
    expect(component.form.valid).toBeTrue();
    component.onSubmit();
    fixture.detectChanges();

    expect(authService.login).toHaveBeenCalledTimes(1);
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
    expect(component.isLoading).toBeFalse();
  });

  it('shows the existing safe message when login fails', () => {
    authService.login.and.returnValue(throwError(() => new Error('fixture HTTP 401')));
    component.form.setValue({ username: 'fixture-user', password: 'x'.repeat(8) });
    fixture.detectChanges();
    expect(component.form.valid).toBeTrue();
    component.onSubmit();
    fixture.detectChanges();
    fixture.detectChanges();

    expect(snackBar.open).toHaveBeenCalledWith('Usuário ou senha inválidos', 'Fechar');
    expect(component.isLoading).toBeFalse();
  });

  it('disables submit while the login request is pending', () => {
    const pending = new Subject<AuthResponse>();
    authService.login.and.returnValue(pending);
    component.form.setValue({ username: 'fixture-user', password: 'x'.repeat(8) });
    fixture.detectChanges();
    component.onSubmit();
    fixture.detectChanges();
    fixture.detectChanges();

    expect(component.isLoading).toBeTrue();
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBeTrue();
    pending.next({ token: 'fixture-token', role: 'MARECHAL' });
    pending.complete();
    expect(component.isLoading).toBeFalse();
  });
});
