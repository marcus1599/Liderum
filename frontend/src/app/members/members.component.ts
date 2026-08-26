import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
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
export class MembersComponent implements OnInit, AfterViewInit {
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
  errorMessage = '';
  statusMessage = '';

  guildRoles: string[] = ['SOLDADO', 'CAPITÃO', 'MAJOR', 'GENERAL', 'MARECHAL'];
  ranks: string[] = ['C', 'B', 'A', 'S'];
  classes: string[] = [
    'GUERREIRO', 'MAGO', 'ATIRADORA', 'SACERDOTE', 'ARQUEIRO', 'PALADINO',
    'BARBARO', 'FEITICEIRA', 'ANDARILHO', 'MERCENARIO', 'ESPIRITUALISTA', 'MISTICO', 'BARDO', 'ARCANO'
  ];

  constructor(private memberService: MemberService) { }

  ngOnInit(): void {
    this.loadMembers();
  }

  loadMembers(): void {
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
    }, error: (error: HttpErrorResponse) => {
      this.loading = false;
      this.errorMessage = error.status === 403 ? 'Você não tem permissão para consultar membros.' : error.status === 404 ? 'Membros não encontrados.' : 'Não foi possível carregar os membros.';
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
    if (this.isEditing) {
      this.memberService.updateMember(this.newMember).subscribe(() => {
        this.loadMembers();
        this.statusMessage = 'Membro atualizado.';
        this.resetForm();
      });
    } else {
      this.memberService.addMember(this.newMember).subscribe(() => {
        this.loadMembers();
        this.statusMessage = 'Membro criado.';
        this.resetForm();
      });
    }
  }

  editMember(member: Member): void {
    this.newMember = { ...member };
    this.isEditing = true;
  }

  deleteMember(id: number): void {
    if (confirm('Tem certeza que deseja excluir este membro?')) {
      this.memberService.deleteMember(id).subscribe(() => {
        this.loadMembers();
      });
    }
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
