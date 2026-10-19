import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
import { MemberService } from '../services/member.service';
import { Member } from './member.model';

import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { HttpErrorResponse } from '@angular/common/http';
import { httpFeedback } from '../shared/http-feedback';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from '../auth/auth.models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-members',
  templateUrl: './members.component.html',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatCardModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatChipsModule,
    MatPaginatorModule,
    MatSortModule
  ],
  styleUrls: ['./members.component.scss']
})
export class MembersComponent implements OnInit, OnDestroy, AfterViewInit {
  members: Member[] = [];
  dataSource = new MatTableDataSource<Member>([]);
  displayedColumns: string[] = ['nickname', 'phone', 'guildRole', 'rank', 'classe', 'actions'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  newMember: Member = {
    id: 0,
    nickname: '',
    phone: '',
    guildRole: 'SOLDADO',
    rank: '',
    classe: 'GUERREIRO'
  };

  isEditing: boolean = false;
  loading = false;
  loaded = false;
  saving = false;
  stale = false;
  errorMessage = '';
  statusMessage = '';
  profile: UserProfile | null = null;
  private profileSubscription?: Subscription;

  guildRoles: string[] = ['SOLDADO', 'CAPITÃO', 'MAJOR', 'GENERAL', 'MARECHAL'];
  ranks: string[] = ['C', 'B', 'A', 'S'];
  classes: string[] = [
    'GUERREIRO', 'MAGO', 'ATIRADORA', 'SACERDOTE', 'ARQUEIRO', 'PALADINO',
    'BARBARO', 'FEITICEIRA', 'ANDARILHO', 'MERCENARIO', 'ESPIRITUALISTA', 'MISTICO', 'BARDO', 'ARCANO'
  ];

  constructor(private memberService: MemberService, private authService: AuthService) { }

  ngOnInit(): void {
    this.profileSubscription = this.authService.profile$.subscribe(profile => {
      this.profile = profile;
      this.displayedColumns = this.canManageDomain()
        ? ['nickname', 'phone', 'guildRole', 'rank', 'classe', 'actions']
        : ['nickname', 'phone', 'guildRole', 'rank', 'classe'];
    });
    this.loadMembers();
  }

  ngOnDestroy(): void { this.profileSubscription?.unsubscribe(); }

  canManageDomain(): boolean {
    return ['MARECHAL', 'GENERAL', 'MAJOR'].includes(this.profile?.guildRole ?? '');
  }

  loadMembers(afterMutation = false): void {
    if (this.loading) return;
    if (!afterMutation) this.statusMessage = '';
    this.loading = true;
    this.errorMessage = '';
    this.memberService.getMembers().subscribe({ next: (data) => {
      this.members = data as Member[];
      this.dataSource.data = data;
      // Atualiza paginator e sort após atualizar os dados
      setTimeout(() => {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
      });
      this.loading = false;
      this.loaded = true;
      this.stale = false;
    }, error: (error: HttpErrorResponse) => {
      this.loading = false;
      this.loaded = false;
      this.stale = afterMutation;
      if (error.status === 401 || error.status === 403) {
        this.members = [];
        this.dataSource.data = [];
      }
      this.errorMessage = afterMutation
        ? `Operação concluída, mas a lista não foi atualizada. ${httpFeedback(error, 'consultar membros', true)}`
        : httpFeedback(error, 'consultar membros', true);
    }});
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  saveMember(): void {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded) return;
    this.saving = true;
    this.errorMessage = '';
    this.statusMessage = '';
    const editing = this.isEditing;
    const operation: Observable<Member> = editing
      ? this.memberService.updateMember({ ...this.newMember })
      : this.memberService.addMember({ ...this.newMember });
    operation.subscribe({
      next: () => {
        this.saving = false;
        this.statusMessage = editing ? 'Membro atualizado.' : 'Membro criado.';
        this.resetForm();
        this.loadMembers(true);
      },
      error: (error: HttpErrorResponse) => {
        this.saving = false;
        this.errorMessage = httpFeedback(error, 'salvar membro');
      }
    });
  }

  editMember(member: Member): void {
    if (!this.canManageDomain()) return;
    this.newMember = { ...member };
    this.isEditing = true;
  }

  deleteMember(id: number): void {
    if (!this.canManageDomain() || this.saving || this.loading || !this.loaded || !confirm('Tem certeza que deseja excluir este membro?')) return;
    this.saving = true;
    this.errorMessage = '';
    this.statusMessage = '';
    this.memberService.deleteMember(id).subscribe({
      next: () => { this.saving = false; this.statusMessage = 'Membro excluído.'; this.loadMembers(true); },
      error: (error: HttpErrorResponse) => { this.saving = false; this.errorMessage = httpFeedback(error, 'excluir membro'); }
    });
  }

  resetForm(): void {
    this.newMember = {
      id: 0,
      nickname: '',
      phone: '',
      guildRole: 'SOLDADO',
      rank: '',
      classe: 'GUERREIRO'
    };
    this.isEditing = false;
  }
}
