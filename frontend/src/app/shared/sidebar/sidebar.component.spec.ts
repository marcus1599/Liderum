import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SidebarComponent } from './sidebar.component';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { BehaviorSubject } from 'rxjs';
import { UserProfile } from '../../auth/auth.models';

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;
  let profile: BehaviorSubject<UserProfile | null>;

  beforeEach(async () => {
    profile = new BehaviorSubject<UserProfile | null>(null);
    await TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [
        provideRouter([
          { path: 'dashboard', component: SidebarComponent },
          { path: 'members', component: SidebarComponent },
          { path: 'teams', component: SidebarComponent },
          { path: 'events', component: SidebarComponent },
          { path: 'attendance', component: SidebarComponent },
          { path: 'users', component: SidebarComponent }
        ]),
        { provide: AuthService, useValue: { profile$: profile.asObservable() } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows Users only after an administrative profile is loaded', () => {
    expect(fixture.nativeElement.querySelector('a[href="/users"]')).toBeNull();

    profile.next({ id: 1, username: 'marechal', email: 'm@example.test', guildRole: 'MARECHAL' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('a[href="/users"]')).not.toBeNull();

    profile.next({ id: 2, username: 'major', email: 'm2@example.test', guildRole: 'MAJOR' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('a[href="/users"]')).toBeNull();
  });

  it('shows Users for GENERAL and keeps it hidden for non-administrative roles', () => {
    profile.next({ id: 3, username: 'general', email: 'g@example.test', guildRole: 'GENERAL' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('a[href="/users"]')).not.toBeNull();

    for (const guildRole of ['MAJOR', 'CAPITÃO', 'SOLDADO']) {
      profile.next({ id: 4, username: 'member', email: 'member@example.test', guildRole });
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('a[href="/users"]')).toBeNull();
    }
  });

  it('marks the current route active and closes the drawer after navigation on handset', async () => {
    const router = TestBed.inject(Router);
    const closeSpy = spyOn(component.closeSidenav, 'emit');
    component.isHandset = true;

    await router.navigateByUrl('/members');
    fixture.detectChanges();

    const membersLink: HTMLAnchorElement = fixture.nativeElement.querySelector('a[href="/members"]');
    expect(membersLink.classList.contains('active')).toBeTrue();
    expect(membersLink.getAttribute('aria-current')).toBe('page');
    membersLink.click();
    expect(closeSpy).toHaveBeenCalled();
  });
});
