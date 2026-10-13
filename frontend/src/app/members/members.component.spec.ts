import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MembersComponent } from './members.component';
import { MemberService } from '../services/member.service';
import { Member } from './member.model';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject } from 'rxjs';

describe('MembersComponent', () => {
  let component: MembersComponent;
  let fixture: ComponentFixture<MembersComponent>;
  let service: jasmine.SpyObj<MemberService>;

  beforeEach(async () => {
    service = jasmine.createSpyObj<MemberService>('MemberService', ['getMembers', 'addMember', 'updateMember', 'deleteMember']);
    service.getMembers.and.returnValue(of([]));
    await TestBed.configureTestingModule({
      imports: [MembersComponent],
      providers: [{ provide: MemberService, useValue: service }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MembersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
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
