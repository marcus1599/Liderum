import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MembersComponent } from './members.component';
import { MemberService } from '../services/member.service';
import { Member } from './member.model';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject } from 'rxjs';
import { BehaviorSubject } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';

describe('MembersComponent', () => {
  let component: MembersComponent;
  let fixture: ComponentFixture<MembersComponent>;
  let service: jasmine.SpyObj<MemberService>;
  let profile$: BehaviorSubject<UserProfile | null>;

  beforeEach(async () => {
    service = jasmine.createSpyObj<MemberService>('MemberService', ['getMembers', 'addMember', 'updateMember', 'deleteMember']);
    service.getMembers.and.returnValue(of([]));
    profile$ = new BehaviorSubject<UserProfile | null>({ id: 1, username: 'admin', email: 'admin@example.test', guildRole: 'MARECHAL' });
    await TestBed.configureTestingModule({
      imports: [MembersComponent],
      providers: [
        { provide: MemberService, useValue: service },
        { provide: AuthService, useValue: { profile$: profile$.asObservable() } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MembersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('keeps member consultation but hides and blocks mutations for a read-only role', () => {
    component.members = [{ id: 4, nickname: 'Hero', phone: '1', guildRole: 'SOLDADO', rank: 'A', classe: 'MAGO' }];
    component.dataSource.data = component.members;
    profile$.next({ id: 2, username: 'soldier', email: 'soldier@example.test', guildRole: 'SOLDADO' });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Hero');
    expect(fixture.nativeElement.textContent).not.toContain('Novo membro');
    expect(component.displayedColumns).not.toContain('actions');
    component.newMember.nickname = 'Blocked';
    component.saveMember();
    component.editMember(component.members[0]);
    component.deleteMember(4);
    expect(service.addMember).not.toHaveBeenCalled();
    expect(service.updateMember).not.toHaveBeenCalled();
    expect(service.deleteMember).not.toHaveBeenCalled();
  });

  it('preserves domain administration for all authorized roles and waits for profile resolution', () => {
    profile$.next(null);
    fixture.detectChanges();
    expect(component.canManageDomain()).toBeFalse();
    expect(component.displayedColumns).not.toContain('actions');
    for (const guildRole of ['MARECHAL', 'GENERAL', 'MAJOR'] as const) {
      profile$.next({ id: 1, username: 'admin', email: 'admin@example.test', guildRole });
      expect(component.canManageDomain()).toBeTrue();
      expect(component.displayedColumns).toContain('actions');
    }
  });

  it('shows an empty state only after a successful read and recovers from a failure', () => {
    service.getMembers.and.returnValue(new Subject<Member[]>());
    component.loadMembers();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Carregando membros');
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum membro cadastrado');
    const failure = new HttpErrorResponse({ status: 403, error: { message: 'secret' } });
    (service.getMembers.calls.mostRecent().returnValue as Subject<Member[]>).error(failure);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('permissão');
    expect(fixture.nativeElement.textContent).not.toContain('secret');
    service.getMembers.and.returnValue(of([]));
    component.loadMembers();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Nenhum membro cadastrado');
    expect(component.errorMessage).toBe('');
  });

  it('prevents duplicate creation and keeps the form when a write fails', () => {
    const pending = new Subject<Member>();
    service.addMember.and.returnValue(pending);
    component.newMember.nickname = 'Hero';
    component.saveMember();
    component.saveMember();
    expect(service.addMember).toHaveBeenCalledTimes(1);
    pending.error(new HttpErrorResponse({ status: 409 }));
    expect(component.saving).toBeFalse();
    expect(component.newMember.nickname).toBe('Hero');
    expect(component.errorMessage).toContain('conflito');
  });

  it('distinguishes a completed write from a failed refresh', () => {
    service.addMember.and.returnValue(of({ ...component.newMember, id: 3 }));
    service.getMembers.and.returnValue(new Subject<Member[]>());
    component.saveMember();
    expect(component.statusMessage).toBe('Membro criado.');
    (service.getMembers.calls.mostRecent().returnValue as Subject<Member[]>).error(new HttpErrorResponse({ status: 500 }));
    expect(component.errorMessage).toContain('Operação concluída');
    expect(component.loaded).toBeFalse();
  });
});
