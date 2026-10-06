import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { aUser } from '../../testing/test-data';
import { SessionStore } from '../services/session-store';
import { authGuard, firstLoginGuard, guestGuard } from './auth-guard';
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

    it('lets authenticated users through', () => {
      session.setUser(aUser());
      expect(run(authGuard)).toBe(true);
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

      session.setUser(aUser({ role: 'ADMIN', permissions: ['user:view'] }));
      expect(run(permissionGuard('user:view'))).toBe(true);
    });
  });
});
