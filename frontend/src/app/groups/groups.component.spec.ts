import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupsComponent } from './groups.component';
import { GroupService } from '../services/group.service';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Group, TeamLeaderCandidate } from '../services/group.service';
import { Member } from '../members/member.model';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { AuthService } from '../auth/auth.service';

describe('GroupsComponent', () => {
  let component: GroupsComponent;
  let fixture: ComponentFixture<GroupsComponent>;
  let service: jasmine.SpyObj<GroupService>;
  let profile$: BehaviorSubject<any>;

  beforeEach(async () => {
    service = jasmine.createSpyObj<GroupService>('GroupService',
      ['getGroups', 'getMembers', 'createGroup', 'updateGroup', 'deleteGroup', 'addMemberToGroup', 'removeMemberFromGroup', 'getLeaderCandidates', 'reassignLeader']);
    service.getGroups.and.returnValue(of([]));
    service.getMembers.and.returnValue(of([]));
    service.getLeaderCandidates.and.returnValue(of([]));
    profile$ = new BehaviorSubject({ id: 1, username: 'admin', email: 'admin@example.test', guildRole: 'MARECHAL' });
    await TestBed.configureTestingModule({
      imports: [GroupsComponent],
      providers: [
        { provide: GroupService, useValue: service },
        { provide: AuthService, useValue: { profile$: profile$.asObservable() } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not delete a group when confirmation is cancelled', () => {
    component.loaded = true;
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteGroup({ id: 8, name: 'Raid', members: [] });
    expect(service.deleteGroup).not.toHaveBeenCalled();
  });

  it('deletes a group only after confirmation', () => {
    component.loaded = true;
    spyOn(window, 'confirm').and.returnValue(true);
    service.deleteGroup.and.returnValue(of(void 0));
    component.deleteGroup({ id: 8, name: 'Raid', members: [] });
    expect(service.deleteGroup).toHaveBeenCalledWith(8);
  });

  it('does not mistake a failed members request for no available members', () => {
    service.getMembers.and.returnValue(new Subject<Member[]>());
    component.loadGroups();
    fixture.detectChanges();
    expect(component.loading).toBeTrue();
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum membro disponível');
    (service.getMembers.calls.mostRecent().returnValue as Subject<Member[]>).error(new HttpErrorResponse({ status: 403 }));
    fixture.detectChanges();
    expect(component.loaded).toBeFalse();
    expect(component.errorMessage).toContain('permissão');
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum membro disponível');
  });

  it('keeps team membership unchanged when drag-and-drop request fails', () => {
    const member = { id: 3, nickname: 'Hero', classe: 'MAGO' } as Member;
    const group: Group = { id: 5, name: 'Raid', members: [] };
    component.groups = [group];
    component.availableMembers = [member];
    const pending = new Subject<void>();
    service.addMemberToGroup.and.returnValue(pending);
    const drop = {
      previousContainer: { data: [member] }, container: { data: group.members }, previousIndex: 0
    } as CdkDragDrop<Member[]>;
    component.dropToGroup(drop, group);
    component.dropToGroup(drop, group);
    expect(service.addMemberToGroup).toHaveBeenCalledTimes(1);
    expect(group.members).toEqual([]);
    expect(component.availableMembers).toEqual([member]);
    pending.error(new HttpErrorResponse({ status: 409 }));
    expect(group.members).toEqual([]);
    expect(component.errorMessage).toContain('conflito');
  });

  it('confirms a write separately from a failed refresh', () => {
    service.createGroup.and.returnValue(of({ id: 2, name: 'Raid', members: [] }));
    service.getGroups.and.returnValue(new Subject<Group[]>());
    component.groupForm.name = 'Raid';
    component.saveGroup();
    expect(component.statusMessage).toBe('Grupo criado.');
    (service.getGroups.calls.mostRecent().returnValue as Subject<Group[]>).error(new HttpErrorResponse({ status: 500 }));
    expect(component.errorMessage).toContain('Operação concluída');
    expect(component.loaded).toBeFalse();
  });

  it('does not move a member until the server confirms add, then reloads', () => {
    const member = { id: 3, nickname: 'Hero' } as Member;
    const group: Group = { id: 5, name: 'Raid', members: [] };
    component.groups = [group];
    component.availableMembers = [member];
    const pending = new Subject<void>();
    service.addMemberToGroup.and.returnValue(pending);
    const drop = {
      previousContainer: { data: [member] }, container: { data: group.members }, previousIndex: 0
    } as CdkDragDrop<Member[]>;
    component.dropToGroup(drop, group);
    expect(group.members).toEqual([]);
    expect(component.availableMembers).toEqual([member]);
    pending.next(); pending.complete();
    expect(component.statusMessage).toBe('Membro adicionado ao grupo.');
    expect(service.getGroups).toHaveBeenCalledTimes(2);
  });

  it('reports failed removal without clearing group membership', () => {
    const member = { id: 3, nickname: 'Hero' } as Member;
    const group: Group = { id: 5, name: 'Raid', members: [member] };
    component.groups = [group];
    service.removeMemberFromGroup.and.returnValue(new Subject<void>());
    component.removeMemberFromGroup(member, group);
    (service.removeMemberFromGroup.calls.mostRecent().returnValue as Subject<void>).error(
      new HttpErrorResponse({ status: 0 }));
    expect(group.members).toEqual([member]);
    expect(component.errorMessage).toContain('Atualize a lista');
  });

  it('loads candidates and changes the displayed leader only after server confirmation', () => {
    const group: Group = { id: 8, name: 'Raid', leaderId: 1, leaderName: 'Old', members: [] };
    component.groups = [group];
    component.loaded = true;
    service.getLeaderCandidates.and.returnValue(of([{ id: 2, username: 'New' }]));
    service.reassignLeader.and.returnValue(of({ ...group, leaderId: 2, leaderName: 'New' }));

    component.beginLeaderReassignment(group);
    expect(component.leaderCandidates).toEqual([{ id: 2, username: 'New' }]);
    component.selectedLeaderId = 2;
    component.confirmLeaderReassignment();

    expect(service.reassignLeader).toHaveBeenCalledWith(8, 2);
    expect(component.groups[0].leaderName).toBe('New');
    expect(component.statusMessage).toContain('transferida');
  });

  it('shows the group and current leader context during reassignment', () => {
    const group: Group = { id: 8, name: 'Raid', leaderId: 1, leaderName: 'Old', members: [] };
    component.groups = [group];
    component.loaded = true;
    service.getLeaderCandidates.and.returnValue(of([{ id: 2, username: 'New' }]));
    component.beginLeaderReassignment(group);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Líder atual: Old');
    expect(fixture.nativeElement.textContent).toContain('Novo líder para Raid');
    expect(fixture.nativeElement.textContent).toContain('A alteração só aparece após confirmação do servidor.');
  });

  it('does not change displayed leader when reassignment fails', () => {
    const group: Group = { id: 8, name: 'Raid', leaderId: 1, leaderName: 'Old', members: [] };
    component.groups = [group];
    component.loaded = true;
    component.leaderCandidates = [{ id: 2, username: 'New' }];
    component.reassigningTeam = group;
    component.selectedLeaderId = 2;
    service.reassignLeader.and.returnValue(new Subject<Group>());

    component.confirmLeaderReassignment();
    (service.reassignLeader.calls.mostRecent().returnValue as Subject<Group>)
      .error(new HttpErrorResponse({ status: 409 }));

    expect(component.groups[0].leaderName).toBe('Old');
    expect(component.saving).toBeFalse();
    expect(component.errorMessage).toContain('conflito');
  });

  it('does not expose reassignment controls to non-admin roles', () => {
    const group: Group = { id: 8, name: 'Raid', members: [] };
    component.groups = [group];
    profile$.next({ id: 3, username: 'soldier', email: 'soldier@example.test', guildRole: 'SOLDADO' });
    fixture.detectChanges();
    expect(component.canReassignLeader()).toBeFalse();
    expect(fixture.nativeElement.textContent).toContain('Raid');
    expect(fixture.nativeElement.textContent).not.toContain('Novo Grupo');
    expect(fixture.nativeElement.querySelector('[aria-label="Editar grupo"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('[aria-label="Excluir grupo"]')).toBeNull();
    component.beginLeaderReassignment({ id: 8, name: 'Raid', members: [] });
    expect(service.getLeaderCandidates).not.toHaveBeenCalled();
    component.groupForm.name = 'Blocked';
    component.saveGroup();
    component.deleteGroup({ id: 8, name: 'Raid', members: [] });
    expect(service.createGroup).not.toHaveBeenCalled();
    expect(service.deleteGroup).not.toHaveBeenCalled();
  });

  it('keeps domain administration available to MARECHAL, GENERAL and MAJOR', () => {
    profile$.next(null);
    expect(component.canManageDomain()).toBeFalse();
    for (const guildRole of ['MARECHAL', 'GENERAL', 'MAJOR'] as const) {
      profile$.next({ id: 1, username: 'admin', email: 'admin@example.test', guildRole });
      expect(component.canManageDomain()).toBeTrue();
    }
  });

  it('provides keyboard-operable member assignment controls to domain administrators', () => {
    const member = { id: 3, nickname: 'Hero', classe: 'MAGO' } as Member;
    component.availableMembers = [member];
    component.groups = [{ id: 5, name: 'Raid', members: [] }];
    component.loaded = true;
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Atribuir membro');
    expect(fixture.nativeElement.textContent).toContain('Escolha um membro disponível e o grupo de destino');
    expect(fixture.nativeElement.textContent).toContain('1. Membro disponível');
    expect(fixture.nativeElement.textContent).toContain('2. Grupo de destino');
    const memberLabel = Array.from(fixture.nativeElement.querySelectorAll('mat-label'))
      .find((label: any) => label.textContent.includes('1. Membro disponível'));
    expect(memberLabel).toBeTruthy();
    const addButton = Array.from(fixture.nativeElement.querySelectorAll('button'))
      .find((button: any) => button.textContent.includes('Adicionar ao grupo')) as HTMLButtonElement;
    expect(addButton).toBeTruthy();
    expect(addButton.disabled).toBeTrue();
  });

  it('does not expose member assignment controls to read-only roles or an unloaded profile', () => {
    component.availableMembers = [{ id: 3, nickname: 'Hero', classe: 'MAGO' } as Member];
    component.groups = [{ id: 5, name: 'Raid', members: [] }];
    component.loaded = true;
    profile$.next(null);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('Atribuir membro');

    profile$.next({ id: 4, username: 'soldier', email: 'soldier@example.test', guildRole: 'SOLDADO' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('Atribuir membro');
    component.selectedAssignmentMemberId = 3;
    component.selectedAssignmentGroupId = 5;
    component.addSelectedMemberToGroup();
    expect(service.addMemberToGroup).not.toHaveBeenCalled();
  });

  it('blocks incomplete, stale, already-assigned and pending member assignment', () => {
    const member = { id: 3, nickname: 'Hero', classe: 'MAGO' } as Member;
    component.availableMembers = [member];
    component.groups = [{ id: 5, name: 'Raid', members: [] }];
    component.loaded = true;
    component.selectedAssignmentMemberId = 3;
    expect(component.canAssignSelectedMember()).toBeFalse();
    component.selectedAssignmentGroupId = 5;
    expect(component.canAssignSelectedMember()).toBeTrue();

    component.stale = true;
    expect(component.canAssignSelectedMember()).toBeFalse();
    component.stale = false;
    component.groups[0].members = [member];
    expect(component.canAssignSelectedMember()).toBeFalse();
    component.groups[0].members = [];
    component.saving = true;
    expect(component.canAssignSelectedMember()).toBeFalse();
  });

  it('adds a selected member once, clears selection after confirmation and reloads', () => {
    const member = { id: 3, nickname: 'Hero', classe: 'MAGO' } as Member;
    component.availableMembers = [member];
    component.groups = [{ id: 5, name: 'Raid', members: [] }];
    component.loaded = true;
    component.selectedAssignmentMemberId = 3;
    component.selectedAssignmentGroupId = 5;
    service.addMemberToGroup.and.returnValue(new Subject<void>());

    component.addSelectedMemberToGroup();
    component.addSelectedMemberToGroup();
    expect(service.addMemberToGroup).toHaveBeenCalledOnceWith(5, 3);
    (service.addMemberToGroup.calls.mostRecent().returnValue as Subject<void>).next();

    expect(component.statusMessage).toBe('Membro adicionado ao grupo.');
    expect(component.selectedAssignmentMemberId).toBeNull();
    expect(component.selectedAssignmentGroupId).toBeNull();
    expect(service.getGroups).toHaveBeenCalledTimes(2);
  });

  it('preserves the assignment selection after a failed request', () => {
    component.availableMembers = [{ id: 3, nickname: 'Hero', classe: 'MAGO' } as Member];
    component.groups = [{ id: 5, name: 'Raid', members: [] }];
    component.loaded = true;
    component.selectedAssignmentMemberId = 3;
    component.selectedAssignmentGroupId = 5;
    service.addMemberToGroup.and.returnValue(new Subject<void>());

    component.addSelectedMemberToGroup();
    (service.addMemberToGroup.calls.mostRecent().returnValue as Subject<void>)
      .error(new HttpErrorResponse({ status: 409 }));

    expect(component.selectedAssignmentMemberId).toBe(3);
    expect(component.selectedAssignmentGroupId).toBe(5);
    expect(component.statusMessage).toBe('');
    expect(component.errorMessage).toContain('conflito');
  });

  it('clears selection after confirmed assignment even if the follow-up reload fails', () => {
    component.availableMembers = [{ id: 3, nickname: 'Hero', classe: 'MAGO' } as Member];
    component.groups = [{ id: 5, name: 'Raid', members: [] }];
    component.loaded = true;
    component.selectedAssignmentMemberId = 3;
    component.selectedAssignmentGroupId = 5;
    service.addMemberToGroup.and.returnValue(of(void 0));
    service.getGroups.and.returnValue(new Subject<Group[]>());

    component.addSelectedMemberToGroup();
    (service.getGroups.calls.mostRecent().returnValue as Subject<Group[]>)
      .error(new HttpErrorResponse({ status: 500 }));

    expect(component.selectedAssignmentMemberId).toBeNull();
    expect(component.selectedAssignmentGroupId).toBeNull();
    expect(component.statusMessage).toBe('Membro adicionado ao grupo.');
    expect(component.errorMessage).toContain('Operação concluída');
  });

  it('does not present a failed candidate request as an empty list', () => {
    const group: Group = { id: 8, name: 'Raid', leaderId: 1, leaderName: 'Current', members: [] };
    component.groups = [group];
    component.loaded = true;
    service.getLeaderCandidates.and.returnValue(new Subject<TeamLeaderCandidate[]>());
    component.beginLeaderReassignment(group);
    (service.getLeaderCandidates.calls.mostRecent().returnValue as Subject<TeamLeaderCandidate[]>)
      .error(new HttpErrorResponse({ status: 500 }));
    fixture.detectChanges();

    expect(component.leaderCandidatesLoaded).toBeFalse();
    expect(fixture.nativeElement.textContent).not.toContain('Nenhum outro usuário ativo disponível');
    expect(component.errorMessage).toContain('carregar os dados');
  });
});
