import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupsComponent } from './groups.component';
import { GroupService } from '../services/group.service';
import { of, Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Group, TeamLeaderCandidate } from '../services/group.service';
import { Member } from '../members/member.model';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { AuthService } from '../auth/auth.service';

describe('GroupsComponent', () => {
  let component: GroupsComponent;
  let fixture: ComponentFixture<GroupsComponent>;
  let service: jasmine.SpyObj<GroupService>;

  beforeEach(async () => {
    service = jasmine.createSpyObj<GroupService>('GroupService',
      ['getGroups', 'getMembers', 'createGroup', 'updateGroup', 'deleteGroup', 'addMemberToGroup', 'removeMemberFromGroup', 'getLeaderCandidates', 'reassignLeader']);
    service.getGroups.and.returnValue(of([]));
    service.getMembers.and.returnValue(of([]));
    service.getLeaderCandidates.and.returnValue(of([]));
    await TestBed.configureTestingModule({
      imports: [GroupsComponent],
      providers: [
        { provide: GroupService, useValue: service },
        { provide: AuthService, useValue: { profile$: of({ id: 1, username: 'admin', email: 'admin@example.test', guildRole: 'MARECHAL' }) } }
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
    component.profile = { id: 3, username: 'soldier', email: 'soldier@example.test', guildRole: 'SOLDADO' };
    fixture.detectChanges();
    expect(component.canReassignLeader()).toBeFalse();
    component.beginLeaderReassignment({ id: 8, name: 'Raid', members: [] });
    expect(service.getLeaderCandidates).not.toHaveBeenCalled();
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
