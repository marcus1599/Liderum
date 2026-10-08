import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';
import { UserManagementService } from './user-management.service';
import { UsersComponent } from './users.component';

describe('UsersComponent', () => {
  let fixture: ComponentFixture<UsersComponent>; let component: UsersComponent;
  let usersService: { list: jasmine.Spy; create: jasmine.Spy; updateRole: jasmine.Spy; remove: jasmine.Spy };
  let profile$: Subject<UserProfile | null>; let snackbar: { open: jasmine.Spy };
  const marechal: UserProfile = { id: 1, username: 'owner', email: 'o@example.com', guildRole: 'MARECHAL' };
  const general: UserProfile = { id: 1, username: 'general', email: 'g@example.com', guildRole: 'GENERAL' };
  beforeEach(async () => {
    profile$ = new Subject<UserProfile | null>();
    usersService = { list: jasmine.createSpy('list').and.returnValue(of([{ id: 2, username: 'soldier', email: 's@example.com', guildRole: 'SOLDADO', status: 'ACTIVE' }])), create: jasmine.createSpy('create').and.returnValue(of({ user: { id: 3, username: 'new', email: 'n@example.com', guildRole: 'SOLDADO', status: 'PENDING' }, activationToken: 'token' })), updateRole: jasmine.createSpy('updateRole').and.returnValue(of({ id: 2, username: 'soldier', email: 's@example.com', guildRole: 'MAJOR', status: 'ACTIVE' })), remove: jasmine.createSpy('remove').and.returnValue(of(void 0)) };
    snackbar = { open: jasmine.createSpy('open') };
    await TestBed.configureTestingModule({ imports: [UsersComponent], providers: [
      { provide: AuthService, useValue: { profile$: profile$.asObservable() } }, { provide: UserManagementService, useValue: usersService },
      { provide: MatSnackBar, useValue: snackbar }, { provide: Router, useValue: {} }, { provide: ActivatedRoute, useValue: {} }
    ] }).compileComponents();
    fixture = TestBed.createComponent(UsersComponent); component = fixture.componentInstance; fixture.detectChanges();
  });
  it('denies management UI to non-administrative roles', () => { profile$.next({ ...marechal, guildRole: 'MAJOR' }); expect(component.canManageUsers()).toBeFalse(); expect(usersService.list).not.toHaveBeenCalled(); });
  it('limits GENERAL create roles and actions against privileged targets', () => { profile$.next(general); expect(component.allowedCreateRoles()).toEqual(['MAJOR', 'CAPITÃO', 'SOLDADO']); expect(component.canActOn({ id: 3, username: 'g', email: 'g', guildRole: 'GENERAL', status: 'ACTIVE' })).toBeFalse(); expect(usersService.list).toHaveBeenCalled(); });
  it('allows MARECHAL to create pending staff without a password', () => { profile$.next(marechal); component.form.setValue({ username: 'new', email: 'n@example.com', role: 'GENERAL' }); component.createUser(); expect(usersService.create).toHaveBeenCalledWith({ username: 'new', email: 'n@example.com', role: 'GENERAL' }); expect(localStorage.getItem('password')).toBeNull(); });
  it('keeps safe error state for forbidden list operations', () => { profile$.next(general); usersService.list.and.returnValue(throwError(() => ({ status: 403 }))); component.loadUsers(); expect(component.errorMessage).toContain('permissão'); });
});
