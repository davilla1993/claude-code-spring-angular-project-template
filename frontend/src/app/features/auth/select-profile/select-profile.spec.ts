import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { SessionStore } from '../../../core/services/session-store';
import { SessionSync } from '../../../core/services/session-sync';
import { aUser } from '../../../testing/test-data';
import { SelectProfile } from './select-profile';

describe('SelectProfile', () => {
  let fixture: ComponentFixture<SelectProfile>;
  let backend: HttpTestingController;
  let session: SessionStore;
  let navigate: ReturnType<typeof vi.spyOn>;
  let announce: ReturnType<typeof vi.spyOn>;

  const buttons = () => [...(fixture.nativeElement as HTMLElement).querySelectorAll('button')];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SelectProfile],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionStore);
    navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    announce = vi.spyOn(TestBed.inject(SessionSync), 'announceSessionChange').mockImplementation(() => undefined);
    session.setUser(aUser({ roles: ['ADMIN', 'USER'], activeRole: null, permissions: [] }));
    fixture = TestBed.createComponent(SelectProfile);
    fixture.detectChanges();
  });

  afterEach(() => backend.verify());

  it('lists the profiles of the user', () => {
    expect(buttons().map((button) => button.textContent?.trim())).toEqual(['Administrateur', 'Utilisateur']);
  });

  it('selects a profile, updates the session, warns other tabs and goes to the requested page', () => {
    fixture.componentRef.setInput('returnUrl', '/app/account');
    buttons()[0].click();

    const request = backend.expectOne('/api/auth/active-profile');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ role: 'ADMIN' });
    request.flush({
      success: true,
      data: aUser({ roles: ['ADMIN', 'USER'], activeRole: 'ADMIN', permissions: ['user:view'] }),
    });

    expect(session.user()?.activeRole).toBe('ADMIN');
    expect(session.hasPermission('user:view')).toBe(true);
    expect(announce).toHaveBeenCalledOnce();
    expect(navigate).toHaveBeenCalledWith('/app/account');
  });

  it('refuses external redirects', () => {
    fixture.componentRef.setInput('returnUrl', '//evil.com');
    buttons()[1].click();

    backend.expectOne('/api/auth/active-profile').flush({ success: true, data: aUser() });

    expect(navigate).toHaveBeenCalledWith('/app');
  });

  it('shows the error and stays on the page when the selection is refused', () => {
    buttons()[0].click();

    backend.expectOne('/api/auth/active-profile').flush(
      { success: false, message: 'Ce profil ne vous est pas attribué.' },
      { status: 403, statusText: 'Forbidden' },
    );
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')?.textContent).toContain(
      'Ce profil ne vous est pas attribué.',
    );
    expect(session.user()?.activeRole).toBeNull();
    expect(navigate).not.toHaveBeenCalled();
    expect(announce).not.toHaveBeenCalled();
  });

  it('does not call the server when choosing the current profile', () => {
    session.setUser(aUser({ roles: ['ADMIN', 'USER'], activeRole: 'USER' }));
    fixture.detectChanges();

    buttons()[1].click();

    backend.expectNone('/api/auth/active-profile');
    expect(navigate).toHaveBeenCalledWith('/app');
  });
});
