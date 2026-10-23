import { Location } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, of, Subject, throwError } from 'rxjs';
import { ActivateComponent } from './activate.component';
import { UserManagementService } from '../users/user-management.service';

describe('ActivateComponent', () => {
  let component: ActivateComponent;
  let fixture: ComponentFixture<ActivateComponent>;
  let service: jasmine.SpyObj<UserManagementService>;
  let router: { navigate: jasmine.Spy };
  let snackBar: { open: jasmine.Spy };
  let location: { replaceState: jasmine.Spy };

  beforeEach(async () => {
    service = jasmine.createSpyObj<UserManagementService>('UserManagementService', ['activate']);
    service.activate.and.returnValue(of(void 0));
    router = { navigate: jasmine.createSpy('navigate') };
    snackBar = { open: jasmine.createSpy('open') };
    location = { replaceState: jasmine.createSpy('replaceState') };

    TestBed.configureTestingModule({
      imports: [ActivateComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: (key: string) => key === 'token' ? 'fixture-token' : null } } }
        },
        { provide: UserManagementService, useValue: service },
        { provide: Router, useValue: router },
        { provide: Location, useValue: location }
      ]
    });
    TestBed.overrideProvider(MatSnackBar, { useValue: snackBar });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(ActivateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders named fields and removes the token from the visible address', () => {
    expect(component).toBeTruthy();
    expect(fixture.nativeElement.querySelector('input[aria-label="Nova senha"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('input[aria-label="Confirmar senha"]')).toBeTruthy();
    expect(location.replaceState).toHaveBeenCalledWith('/activate');
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBeTrue();
  });

  it('announces mismatched confirmation and keeps activation unavailable', () => {
    component.form.setValue({ password: 'x'.repeat(8), confirmation: 'different' });
    component.form.get('confirmation')?.markAsTouched();
    fixture.detectChanges();

    const confirmation: HTMLInputElement = fixture.nativeElement.querySelector('input[aria-label="Confirmar senha"]');
    const message: HTMLElement = fixture.nativeElement.querySelector('#confirmation-error');
    expect(message.textContent).toContain('As senhas não conferem.');
    expect(confirmation.getAttribute('aria-describedby')).toBe('confirmation-error');
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBeTrue();
  });

  it('preserves the invalid-link message when the service rejects activation', () => {
    service.activate.and.returnValue(throwError(() => new Error('fixture HTTP 400')));
    component.form.setValue({ password: 'x'.repeat(8), confirmation: 'x'.repeat(8) });
    fixture.detectChanges();
    expect(component.form.valid).toBeTrue();
    component.submit();
    fixture.detectChanges();
    fixture.detectChanges();

    expect(service.activate).toHaveBeenCalledWith('fixture-token', 'x'.repeat(8));
    expect(snackBar.open).toHaveBeenCalledWith('Link inválido ou expirado.', 'Fechar');
    expect(component.saving).toBeFalse();
  });

  it('confirms success and returns the user to login', () => {
    component.form.setValue({ password: 'x'.repeat(8), confirmation: 'x'.repeat(8) });
    fixture.detectChanges();
    component.submit();
    fixture.detectChanges();

    expect(snackBar.open).toHaveBeenCalledWith('Conta ativada. Faça login.', 'Fechar');
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('keeps submit disabled while activation is pending', () => {
    const pending = new Subject<void>();
    service.activate.and.returnValue(pending as Observable<void>);
    component.form.setValue({ password: 'x'.repeat(8), confirmation: 'x'.repeat(8) });
    fixture.detectChanges();
    component.submit();
    fixture.detectChanges();
    fixture.detectChanges();

    expect(component.saving).toBeTrue();
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBeTrue();
    pending.next();
    pending.complete();
  });
});
