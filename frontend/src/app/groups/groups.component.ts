import { Component, OnInit } from '@angular/core';
import { CdkDragDrop, CdkDropList, DragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { GroupService, Group } from '../services/group.service';
import { MatTable } from '@angular/material/table';
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



@Component({
  selector: 'app-groups',
  templateUrl: './groups.component.html',
  styleUrls: ['./groups.component.scss'],
  imports: [
     MatTable,
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
export class GroupsComponent implements OnInit {
  availableMembers: Member[] = [];
  groups: Group[] = [];
  showGroupForm = false;
  groupForm: Partial<Group> = {};
  editingGroup: Group | null = null;
  filterText: string = '';

  constructor(private groupService: GroupService) {}

  ngOnInit() {
    this.loadGroups();
    this.loadClasses(); // Adicione esta linha
  }

  loadMembers() {
    this.groupService.getMembers().subscribe(members => {
      const groupMemberIds = this.groups.flatMap(g => g.members.map(m => m.id));
      this.availableMembers = members.filter(m => !groupMemberIds.includes(m.id));
      this.dataSource.data = this.availableMembers; // <-- Atualize aqui
    });
  }
  @ViewChild(MatPaginator) paginator!: MatPaginator;

ngAfterViewInit() {
  this.dataSource.paginator = this.paginator;
}

  loadGroups() {
    this.groupService.getGroups().subscribe(groups => {
      // Garante que todos os grupos tenham members como array
      this.groups = groups.map(g => ({
        ...g,
        members: Array.isArray(g.members) ? g.members : []
      }));
      this.loadMembers();
    });
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
    if (this.editingGroup) {
      this.groupService.updateGroup({ ...this.editingGroup, name: this.groupForm.name! }).subscribe(() => {
        this.loadGroups();
        this.cancelGroupForm();
      });
    } else {
      this.groupService.createGroup({ name: this.groupForm.name! }).subscribe(() => {
        this.loadGroups();
        this.cancelGroupForm();
      });
    }
  }

  cancelGroupForm() {
    this.showGroupForm = false;
    this.groupForm = {};
    this.editingGroup = null;
  }

  editGroup(group: Group) {
    this.openGroupForm(group);
  }

  deleteGroup(group: Group) {
    this.groupService.deleteGroup(group.id).subscribe(() => {
      this.loadGroups();
    });
  }

dropToGroup(event: CdkDragDrop<Member[]>, group: Group) {
  if (event.previousContainer === event.container) {
    moveItemInArray(group.members, event.previousIndex, event.currentIndex);
  } else {
    const member = event.previousContainer.data[event.previousIndex];
    if (!group.members.some(m => m.id === member.id)) {
      // Chame o serviço para adicionar o membro ao grupo no backend
      this.groupService.addMemberToGroup(group.id, member.id).subscribe(() => {
        // Após salvar no backend, recarregue os grupos para garantir consistência
        this.loadGroups();
      });
      // Opcional: atualize a UI imediatamente (otimista)
      group.members.push(member);
      this.availableMembers = this.availableMembers.filter(m => m.id !== member.id);
      this.filterMembers();
    }
  }
}

  dropToAvailable(event: CdkDragDrop<Member[]>) {
    if (event.previousContainer !== event.container) {
      const member = event.previousContainer.data[event.previousIndex];
      // Descobre de qual grupo veio
      const group = this.groups.find(g => g.members.some(m => m.id === member.id));
      if (group) {
        this.groupService.removeMemberFromGroup(group.id, member.id).subscribe(() => {
          this.loadGroups();
        });
      }
    }
  }

  removeMemberFromGroup(member: Member, group: Group) {
    this.groupService.removeMemberFromGroup(group.id, member.id).subscribe(() => {
      this.loadGroups();
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


