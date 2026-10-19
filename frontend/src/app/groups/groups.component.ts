import { Component, OnDestroy, OnInit } from '@angular/core';
import { CdkDragDrop, CdkDropList, DragDropModule } from '@angular/cdk/drag-drop';
import { GroupService, Group, TeamLeaderCandidate } from '../services/group.service';
import { MatButton } from '@angular/material/button';
import { MatCard, MatCardModule } from '@angular/material/card';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { MatFormField, MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatDividerModule } from '@angular/material/divider';
import { Member } from '../members/member.model';
import { MatTableDataSource } from '@angular/material/table';
import { ViewChild } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { httpFeedback } from '../shared/http-feedback';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';
import { Subscription } from 'rxjs';



@Component({
  selector: 'app-groups',
  templateUrl: './groups.component.html',
  styleUrls: ['./groups.component.scss'],
  imports: [
     MatButton, 
     MatCard, 
     CdkDropList, 
     FormsModule, 
     ReactiveFormsModule, 
     CommonModule, 
     MatFormFieldModule,
     MatSelectModule, 
     MatFormFieldModule, 
     MatInputModule, 
     MatLabel,
     MatFormField, 
     DragDropModule,
     MatIconModule, 
     MatChipsModule, 
     MatPaginatorModule, 
     MatSortModule,
    MatCardModule,
     MatDividerModule,

    ]
})
export class GroupsComponent implements OnInit, OnDestroy {
  availableMembers: Member[] = [];
  groups: Group[] = [];
  showGroupForm = false;
  groupForm: Partial<Group> = {};
  editingGroup: Group | null = null;
  filterText: string = '';
  loading = false;
  loaded = false;
  saving = false;
  stale = false;
  errorMessage = '';
  statusMessage = '';
  profile: UserProfile | null = null;
  leaderCandidates: TeamLeaderCandidate[] = [];
  leaderCandidatesLoading = false;
  leaderCandidatesLoaded = false;
  selectedLeaderId: number | null = null;
  reassigningTeam: Group | null = null;
  private profileSubscription?: Subscription;

  constructor(private groupService: GroupService, private authService: AuthService) {}

  ngOnInit() {
    this.profileSubscription = this.authService.profile$.subscribe(profile => this.profile = profile);
    this.loadGroups();
  }

  ngOnDestroy(): void { this.profileSubscription?.unsubscribe(); }

  canReassignLeader(): boolean {
    return this.canManageDomain();
  }

  canManageDomain(): boolean {
    return ['MARECHAL', 'GENERAL', 'MAJOR'].includes(this.profile?.guildRole ?? '');
  }

  beginLeaderReassignment(group: Group): void {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
    this.reassigningTeam = group;
    this.selectedLeaderId = group.leaderId ?? null;
    this.leaderCandidates = [];
    this.leaderCandidatesLoading = true;
    this.leaderCandidatesLoaded = false;
    this.errorMessage = '';
    this.groupService.getLeaderCandidates().subscribe({
      next: candidates => {
        this.leaderCandidates = candidates.filter(candidate => candidate.id !== group.leaderId);
        this.leaderCandidatesLoading = false;
        this.leaderCandidatesLoaded = true;
      },
      error: (error: HttpErrorResponse) => {
        this.leaderCandidatesLoading = false;
        this.errorMessage = httpFeedback(error, 'consultar candidatos a líder', true);
      }
    });
  }

  cancelLeaderReassignment(): void {
    if (this.saving) return;
    this.reassigningTeam = null;
    this.selectedLeaderId = null;
    this.leaderCandidates = [];
    this.leaderCandidatesLoaded = false;
  }

  confirmLeaderReassignment(): void {
    const team = this.reassigningTeam;
    const leaderId = this.selectedLeaderId;
    if (!team || !this.canManageDomain() || leaderId == null || leaderId === team.leaderId || this.saving || this.leaderCandidatesLoading) return;
    this.saving = true;
    this.errorMessage = '';
    this.statusMessage = '';
    this.groupService.reassignLeader(team.id, leaderId).subscribe({
      next: updated => {
        this.groups = this.groups.map(group => group.id === updated.id ? {
          ...group,
          leaderId: updated.leaderId ?? leaderId,
          leaderName: updated.leaderName ?? this.leaderCandidates.find(candidate => candidate.id === leaderId)?.username ?? null
        } : group);
        this.saving = false;
        this.statusMessage = `Liderança de ${team.name} transferida.`;
        this.cancelLeaderReassignment();
      },
      error: (error: HttpErrorResponse) => {
        this.saving = false;
        this.errorMessage = httpFeedback(error, 'transferir liderança');
      }
    });
  }

  loadMembers(afterMutation = false) {
    this.groupService.getMembers().subscribe({ next: members => {
      const groupMemberIds = this.groups.flatMap(g => g.members.map(m => m.id));
      this.availableMembers = members.filter(m => !groupMemberIds.includes(m.id));
      this.filterMembers();
      this.loaded = true;
      this.loading = false;
      this.stale = false;
    }, error: (error: HttpErrorResponse) => {
      this.loading = false;
      this.loaded = false;
      this.stale = afterMutation;
      if (error.status === 401 || error.status === 403) {
        this.groups = [];
        this.availableMembers = [];
        this.dataSource.data = [];
      }
      this.errorMessage = afterMutation
        ? `Operação concluída, mas os dados não foram atualizados. ${httpFeedback(error, 'consultar membros', true)}`
        : httpFeedback(error, 'consultar membros', true);
    }});
  }
  @ViewChild(MatPaginator) paginator!: MatPaginator;

ngAfterViewInit() {
  this.dataSource.paginator = this.paginator;
}

  loadGroups(afterMutation = false) {
    if (this.loading) return;
    if (!afterMutation) this.statusMessage = '';
    this.loading = true;
    this.loaded = false;
    this.errorMessage = '';
    this.groupService.getGroups().subscribe({ next: groups => {
      // Garante que todos os grupos tenham members como array
      this.groups = groups.map(g => ({
        ...g,
        members: Array.isArray(g.members) ? g.members : []
      }));
      this.loadMembers(afterMutation);
    }, error: (error: HttpErrorResponse) => {
      this.loading = false;
      this.stale = afterMutation;
      if (error.status === 401 || error.status === 403) {
        this.groups = [];
        this.availableMembers = [];
        this.dataSource.data = [];
      }
      this.errorMessage = afterMutation
        ? `Operação concluída, mas os dados não foram atualizados. ${httpFeedback(error, 'consultar grupos', true)}`
        : httpFeedback(error, 'consultar grupos', true);
    }});
  }
  filterMembers() {
  this.dataSource.data = this.availableMembers.filter(member =>
    (!this.selectedClass || member.classe === this.selectedClass) &&
    (!this.filterText || member.nickname.toLowerCase().includes(this.filterText))
  );
}
  applyFilter(event: Event) {
  this.filterText = (event.target as HTMLInputElement).value.trim().toLowerCase();
  this.filterMembers();
  }


  openGroupForm(group?: Group) {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
    this.showGroupForm = true;
    if (group) {
      this.groupForm = { ...group };
      this.editingGroup = group;
    } else {
      this.groupForm = {};
      this.editingGroup = null;
    }
  }

  saveGroup() {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded || !this.groupForm.name?.trim()) return;
    this.saving = true;
    this.errorMessage = '';
    this.statusMessage = '';
    const editing = !!this.editingGroup;
    const operation: Observable<Group> = this.editingGroup
      ? this.groupService.updateGroup(this.editingGroup.id, { name: this.groupForm.name })
      : this.groupService.createGroup({ name: this.groupForm.name });
    operation.subscribe({
      next: () => {
        this.saving = false;
        this.statusMessage = editing ? 'Grupo atualizado.' : 'Grupo criado.';
        this.cancelGroupForm();
        this.loadGroups(true);
      },
      error: (error: HttpErrorResponse) => { this.saving = false; this.errorMessage = httpFeedback(error, 'salvar grupo'); }
    });
  }

  cancelGroupForm() {
    if (this.saving) return;
    this.showGroupForm = false;
    this.groupForm = {};
    this.editingGroup = null;
  }

  editGroup(group: Group) {
    this.openGroupForm(group);
  }

  deleteGroup(group: Group) {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
    this.mutate(this.groupService.deleteGroup(group.id), 'Grupo excluído.', 'excluir grupo');
  }

dropToGroup(event: CdkDragDrop<Member[]>, group: Group) {
  if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
  if (event.previousContainer === event.container) {
    return; // A API não persiste ordenação.
  } else {
    const member = event.previousContainer.data[event.previousIndex];
    if (!group.members.some(m => m.id === member.id)) {
      this.mutate(this.groupService.addMemberToGroup(group.id, member.id), 'Membro adicionado ao grupo.', 'adicionar membro ao grupo');
    }
  }
}

  dropToAvailable(event: CdkDragDrop<Member[]>) {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
    if (event.previousContainer !== event.container) {
      const member = event.previousContainer.data[event.previousIndex];
      // Descobre de qual grupo veio
      const group = this.groups.find(g => g.members.some(m => m.id === member.id));
      if (group) {
        this.removeMemberFromGroup(member, group);
      }
    }
  }

  removeMemberFromGroup(member: Member, group: Group) {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
    this.mutate(this.groupService.removeMemberFromGroup(group.id, member.id), 'Membro removido do grupo.', 'remover membro do grupo');
  }

  private mutate(operation: Observable<unknown>, success: string, action: string): void {
    this.saving = true;
    this.errorMessage = '';
    this.statusMessage = '';
    operation.subscribe({
      next: () => { this.saving = false; this.statusMessage = success; this.loadGroups(true); },
      error: (error: HttpErrorResponse) => { this.saving = false; this.errorMessage = httpFeedback(error, action); }
    });
  }

  get groupDropListIds(): string[] {
    return this.groups.map(g => 'groupDropList-' + g.id);
  }

  selectedClass: string = '';
  classes: string[] = [
  'GUERREIRO',
  'MAGO',
  'ATIRADORA',
  'SACERDOTE',
  'ARQUEIRO',
  'PALADINO',
  'BARBARO',
  'FEITICEIRA',
  'ANDARILHO',
  'MERCENARIO',
  'ESPIRITUALISTA',
  'MISTICO',
  'ARCANO'
  ];

  dataSource: MatTableDataSource<Member> = new MatTableDataSource<Member>();

  loadClasses() {
 
  }

  filterMembersByClass() {
    this.filterMembers();
  }
}

