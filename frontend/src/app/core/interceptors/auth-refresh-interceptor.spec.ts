import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { aUser } from '../../testing/test-data';
import { SessionStore } from '../services/session-store';
import { authRefreshInterceptor } from './auth-refresh-interceptor';

describe('authRefreshInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let session: SessionStore;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authRefreshInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionStore);
    router = TestBed.inject(Router);
  });

  afterEach(() => backend.verify());

  it('refreshes the session then replays the request on 401', () => {
    let result: unknown;
    http.get('/api/users').subscribe((body) => (result = body));

    backend.expectOne('/api/users').flush(null, { status: 401, statusText: 'Unauthorized' });
    backend.expectOne('/api/auth/refresh').flush({ success: true, data: aUser() });
    backend.expectOne('/api/users').flush({ success: true, data: 'ok' });

    expect(result).toEqual({ success: true, data: 'ok' });
    expect(session.user()?.publicId).toBe('user-1');
  });

  it('shares a single refresh between concurrent 401 responses', () => {
    http.get('/api/a').subscribe();
    http.get('/api/b').subscribe();

    backend.expectOne('/api/a').flush(null, { status: 401, statusText: 'Unauthorized' });
    backend.expectOne('/api/b').flush(null, { status: 401, statusText: 'Unauthorized' });

    backend.expectOne('/api/auth/refresh').flush({ success: true, data: aUser() });
    backend.expectOne('/api/a').flush({});
    backend.expectOne('/api/b').flush({});
  });

  it('clears the session and redirects to login when the refresh fails', () => {
    session.setUser(aUser());
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    let status: number | undefined;
    http.get('/api/users').subscribe({ error: (err) => (status = err.status) });

    backend.expectOne('/api/users').flush(null, { status: 401, statusText: 'Unauthorized' });
    backend.expectOne('/api/auth/refresh').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(status).toBe(401);
    expect(session.user()).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/auth/login'], expect.anything());
  });

  it('never refreshes for /auth endpoints', () => {
    let status: number | undefined;
    http.post('/api/auth/login', {}).subscribe({ error: (err) => (status = err.status) });

    backend.expectOne('/api/auth/login').flush(null, { status: 401, statusText: 'Unauthorized' });

    backend.expectNone('/api/auth/refresh');
    expect(status).toBe(401);
  });

  it('lets non-401 errors through untouched', () => {
    let status: number | undefined;
    http.get('/api/users').subscribe({ error: (err) => (status = err.status) });

    backend.expectOne('/api/users').flush(null, { status: 403, statusText: 'Forbidden' });

    backend.expectNone('/api/auth/refresh');
    expect(status).toBe(403);
  });
});
