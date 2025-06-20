import { Component, OnInit } from '@angular/core';
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
    MatInputModule
  ],
  styleUrls: ['./members.component.scss']
})
export class MembersComponent implements OnInit {
  members: Member[] = [];  
  displayedColumns: string[] = ['nickname', 'phone', 'guildRole', 'rank', 'classe', 'actions'];  

  newMember: Member = {
    id: 0,
    nickname: '',
    phone: '',
    guildRole: 'SOLDIER',
    rank: '',
    classe: 'Guerreiro'
  };

  isEditing: boolean = false;

  guildRoles: string[] = ['SOLDADO', 'CAPITÃO', 'MAJOR', 'GENERAL', 'MARECHAL'];
  ranks: string[] = ['C', 'B', 'A', 'S'];
  classes: string[] = [
    'GUERREIRO', 'MAGO', 'ATIRADORA', 'SACERDOTE', 'ARQUEIRO', 'PALADINO',
    'BARBARO', 'FEITICEIRA', 'MACACO', 'MERCENARIO', 'ESPIRITUALISTA', 'MISTICO', 'ARCANO'
  ];

  constructor(private memberService: MemberService) { }

  ngOnInit(): void {
    this.loadMembers(); 
  }

  loadMembers(): void {
    this.memberService.getMembers().subscribe((data) => {
      this.members = data;
    });
  }

  saveMember(): void {
    if (this.isEditing) {
      this.memberService.updateMember(this.newMember).subscribe(() => {
        this.loadMembers();
        this.resetForm();
      });
    } else {
      this.memberService.addMember(this.newMember).subscribe(() => {
        this.loadMembers();
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
      guildRole: 'SOLDIER',
      rank: '',
      classe: 'Guerreiro'
    };
    this.isEditing = false;
  }
}
