import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { aUser } from '../../testing/test-data';
import { SessionStore } from '../services/session-store';
import { authGuard, firstLoginGuard, guestGuard, profileSelectionGuard } from './auth-guard';
import { permissionGuard } from './permission-guard';

describe('route guards', () => {
  let session: SessionStore;
  let router: Router;
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/app/account' } as RouterStateSnapshot;

  const run = (guard: typeof authGuard) => TestBed.runInInjectionContext(() => guard(route, state));
  const urlOf = (result: unknown) => router.serializeUrl(result as UrlTree);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    session = TestBed.inject(SessionStore);
    router = TestBed.inject(Router);
  });

  describe('authGuard', () => {
    it('redirects anonymous users to login with the requested url', () => {
      expect(urlOf(run(authGuard))).toBe('/auth/login?returnUrl=%2Fapp%2Faccount');
    });

    it('forces password setup while the temporary password is in use', () => {
      session.setUser(aUser({ firstLogin: true }));
      expect(urlOf(run(authGuard))).toBe('/auth/setup-password');
    });

    it('asks for a profile while none is active, keeping the requested url', () => {
      session.setUser(aUser({ roles: ['ADMIN', 'USER'], activeRole: null }));
      expect(urlOf(run(authGuard))).toBe('/auth/select-profile?returnUrl=%2Fapp%2Faccount');
    });

    it('lets authenticated users through', () => {
      session.setUser(aUser());
      expect(run(authGuard)).toBe(true);
    });
  });

  describe('profileSelectionGuard', () => {
    it('sends anonymous users to login', () => {
      expect(urlOf(run(profileSelectionGuard))).toBe('/auth/login');
    });

    it('forces password setup first', () => {
      session.setUser(aUser({ firstLogin: true, roles: ['ADMIN', 'USER'], activeRole: null }));
      expect(urlOf(run(profileSelectionGuard))).toBe('/auth/setup-password');
    });

    it('allows users with several roles, even with an active profile', () => {
      session.setUser(aUser({ roles: ['ADMIN', 'USER'], activeRole: 'USER' }));
      expect(run(profileSelectionGuard)).toBe(true);
    });

    it('sends users with a single active role to the application', () => {
      session.setUser(aUser());
      expect(urlOf(run(profileSelectionGuard))).toBe('/app');
    });

    it('does not loop when a single role has no active profile', () => {
      session.setUser(aUser({ activeRole: null }));
      expect(run(profileSelectionGuard)).toBe(true);
    });
  });

  describe('firstLoginGuard', () => {
    it('only allows users who still have a temporary password', () => {
      session.setUser(aUser({ firstLogin: true }));
      expect(run(firstLoginGuard)).toBe(true);

      session.setUser(aUser({ firstLogin: false }));
      expect(urlOf(run(firstLoginGuard))).toBe('/app');
    });
  });

  describe('guestGuard', () => {
    it('sends authenticated users to the application', () => {
      expect(run(guestGuard)).toBe(true);
      session.setUser(aUser());
      expect(urlOf(run(guestGuard))).toBe('/app');
    });
  });

  describe('permissionGuard', () => {
    it('requires the permission to be granted to the current user', () => {
      session.setUser(aUser({ permissions: [] }));
      expect(urlOf(run(permissionGuard('user:view')))).toBe('/app');

      session.setUser(aUser({ activeRole: 'ADMIN', permissions: ['user:view'] }));
      expect(run(permissionGuard('user:view'))).toBe(true);
    });
  });
});
