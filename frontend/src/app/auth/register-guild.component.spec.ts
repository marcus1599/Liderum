import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { RegisterGuildComponent } from './register-guild.component';
import { GuildRegistrationService } from './guild-registration.service';

describe('RegisterGuildComponent', () => {
  let component: RegisterGuildComponent;
  let fixture: ComponentFixture<RegisterGuildComponent>;
  let registration: { register: jasmine.Spy };
  let router: { navigate: jasmine.Spy; createUrlTree: jasmine.Spy; serializeUrl: jasmine.Spy; events: ReturnType<typeof of> };
  let snackbar: MatSnackBar;

  beforeEach(async () => {
    registration = { register: jasmine.createSpy('register').and.returnValue(of({ id: 1, username: 'owner', email: 'owner@example.com', guildRole: 'MARECHAL' })) };
    router = { navigate: jasmine.createSpy('navigate'), createUrlTree: jasmine.createSpy('createUrlTree').and.returnValue({}), serializeUrl: jasmine.createSpy('serializeUrl').and.returnValue('/'), events: of() };
    await TestBed.configureTestingModule({
      imports: [RegisterGuildComponent],
      providers: [
        { provide: GuildRegistrationService, useValue: registration },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(RegisterGuildComponent);
    component = fixture.componentInstance;
    snackbar = fixture.debugElement.injector.get(MatSnackBar);
    spyOn(snackbar, 'open').and.returnValue({} as never);
    fixture.detectChanges();
  });

  function fillForm(): void {
    component.form.setValue({ guildName: 'Guild', serverName: 'Server', username: 'owner', email: 'owner@example.com', password: 'password123', confirmPassword: 'password123' });
  }

  it('starts invalid and validates email and confirmation', () => {
    expect(component.form.invalid).toBeTrue();
    component.form.patchValue({ email: 'invalid', password: 'password123', confirmPassword: 'different' });
    expect(component.form.invalid).toBeTrue();
    expect(component.form.hasError('passwordMismatch')).toBeTrue();
  });

  it('submits the contract once, gives feedback and navigates without creating a session', () => {
    fillForm();
    component.submit();
    component.submit();
    expect(registration.register).toHaveBeenCalledTimes(1);
    expect(registration.register).toHaveBeenCalledWith({ guildName: 'Guild', serverName: 'Server', username: 'owner', email: 'owner@example.com', password: 'password123' });
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    expect(snackbar.open).toHaveBeenCalled();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('restores loading and shows a safe message for backend errors', () => {
    registration.register.and.returnValue(throwError(() => ({ status: 500, error: { message: 'internal details' } })));
    fillForm();
    component.submit();
    expect(component.isLoading).toBeFalse();
    expect(snackbar.open).toHaveBeenCalledWith(jasmine.stringMatching('Tente novamente'), 'Fechar');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('keeps the user on the form and gives safe feedback for 4xx and network errors', () => {
    registration.register.and.returnValue(throwError(() => ({ status: 400, error: { errors: { email: 'internal detail' } } })));
    fillForm();
    component.submit();
    expect(component.isLoading).toBeFalse();
    expect(snackbar.open).toHaveBeenCalledWith(jasmine.stringMatching('Confira os dados'), 'Fechar');
    expect(router.navigate).not.toHaveBeenCalled();

    (snackbar.open as jasmine.Spy).calls.reset();
    registration.register.and.returnValue(throwError(() => ({ status: 0 })));
    fillForm();
    component.submit();
    expect(component.isLoading).toBeFalse();
    expect(snackbar.open).toHaveBeenCalledWith(jasmine.stringMatching('Tente novamente'), 'Fechar');
  });

  it('does not submit an invalid form', () => {
    component.submit();
    expect(registration.register).not.toHaveBeenCalled();
  });
});
