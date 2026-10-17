import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { AuthService } from './auth.service';
import { PasswordChangeComponent } from './password-change.component';

describe('PasswordChangeComponent', () => {
  let fixture: ComponentFixture<PasswordChangeComponent>;
  let component: PasswordChangeComponent;
  let auth: jasmine.SpyObj<AuthService>;
  let snack: MatSnackBar;

  beforeEach(async () => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['changeOwnPassword', 'logout']);
    await TestBed.configureTestingModule({
      imports: [PasswordChangeComponent],
      providers: [
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(PasswordChangeComponent);
    component = fixture.componentInstance;
    snack = fixture.debugElement.injector.get(MatSnackBar);
    spyOn(snack, 'open').and.returnValue({} as never);
    fixture.detectChanges();
  });

  it('validates matching passwords and rejects values beyond bcrypt UTF-8 byte limit', () => {
    component.form.setValue({ currentPassword: 'current', newPassword: 'replacement123', confirmation: 'different' });
    expect(component.form.hasError('passwordMismatch')).toBeTrue();
    component.form.patchValue({ newPassword: 'é'.repeat(37), confirmation: 'é'.repeat(37) });
    expect(component.form.controls.newPassword.hasError('bcryptByteLimit')).toBeTrue();
    expect(auth.changeOwnPassword).not.toHaveBeenCalled();
  });

  it('submits only the two password fields and clears the session after confirmed success', () => {
    const pending = new Subject<void>();
    auth.changeOwnPassword.and.returnValue(pending);
    component.form.setValue({ currentPassword: 'current-secret', newPassword: 'replacement-secret', confirmation: 'replacement-secret' });
    component.submit();
    component.submit();
    expect(auth.changeOwnPassword).toHaveBeenCalledTimes(1);
    expect(auth.changeOwnPassword).toHaveBeenCalledWith({ currentPassword: 'current-secret', newPassword: 'replacement-secret' });
    expect(component.saving).toBeTrue();
    pending.next();
    pending.complete();
    expect(component.saving).toBeFalse();
    expect(auth.logout).toHaveBeenCalledTimes(1);
    expect(snack.open).toHaveBeenCalledWith('Senha alterada. Entre novamente com a nova senha.', 'Fechar');
    expect(component.form.getRawValue()).toEqual({ currentPassword: '', newPassword: '', confirmation: '' });
  });

  it('clears only the rejected current password for a safe 400 response', () => {
    auth.changeOwnPassword.and.returnValue(new Subject<void>().pipe());
    component.form.setValue({ currentPassword: 'wrong-current', newPassword: 'replacement-secret', confirmation: 'replacement-secret' });
    const request = new Subject<void>();
    auth.changeOwnPassword.and.returnValue(request);
    component.submit();
    request.error(new HttpErrorResponse({ status: 400, error: { message: 'CURRENT_PASSWORD_INVALID', password: 'must-not-render' } }));
    fixture.detectChanges();
    expect(component.form.controls.currentPassword.value).toBe('');
    expect(component.form.controls.newPassword.value).toBe('replacement-secret');
    expect(fixture.nativeElement.textContent).toContain('A senha atual não confere');
    expect(fixture.nativeElement.textContent).not.toContain('must-not-render');
    expect(auth.logout).not.toHaveBeenCalled();
  });

  it('blocks retry and requires login when the write result is ambiguous', () => {
    auth.changeOwnPassword.and.returnValue(new Subject<void>());
    component.form.setValue({ currentPassword: 'current-secret', newPassword: 'replacement-secret', confirmation: 'replacement-secret' });
    const request = new Subject<void>();
    auth.changeOwnPassword.and.returnValue(request);
    component.submit();
    request.error(new HttpErrorResponse({ status: 0, error: { message: 'sensitive response' } }));
    fixture.detectChanges();
    expect(component.requiresReauthentication).toBeTrue();
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBeTrue();
    expect(fixture.nativeElement.textContent).not.toContain('sensitive response');
    component.goToLogin();
    expect(auth.logout).toHaveBeenCalledTimes(1);
    expect(component.form.getRawValue()).toEqual({ currentPassword: '', newPassword: '', confirmation: '' });
  });
});
