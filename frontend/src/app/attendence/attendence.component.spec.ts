import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendenceComponent } from './attendence.component';
import { MemberService } from '../services/member.service';
import { EventService } from '../services/event.service';
import { AttendanceService } from '../services/attendance.service';
import { of } from 'rxjs';

describe('AttendenceComponent', () => {
  let component: AttendenceComponent;
  let fixture: ComponentFixture<AttendenceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttendenceComponent],
      providers: [
        { provide: MemberService, useValue: { getMembers: () => of([]) } },
        { provide: EventService, useValue: { getEvents: () => of([]) } },
        {
          provide: AttendanceService,
          useValue: {
            getAttendances: () => of([]),
            getMembersWithConsecutiveAbsences: () => of([])
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttendenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
