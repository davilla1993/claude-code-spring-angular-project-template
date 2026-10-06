import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { SessionStore } from '../../../core/services/session-store';
import { aUser } from '../../../testing/test-data';
import { Login } from './login';

describe('Login', () => {
  let backend: HttpTestingController;
  let router: Router;
  let navigateByUrl: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    navigateByUrl = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
  });

  afterEach(() => backend.verify());

  async function submit(returnUrl?: string): Promise<ComponentFixture<Login>> {
    const fixture = TestBed.createComponent(Login);
    if (returnUrl) {
      fixture.componentRef.setInput('returnUrl', returnUrl);
    }
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    const email = element.querySelector<HTMLInputElement>('#login-email')!;
    const password = element.querySelector<HTMLInputElement>('#login-password')!;
    email.value = 'jane@example.com';
    email.dispatchEvent(new Event('input'));
    password.value = 'Secret#123';
    password.dispatchEvent(new Event('input'));
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    return fixture;
  }

  it('does not call the API when the form is invalid', async () => {
    const fixture = TestBed.createComponent(Login);
    await fixture.whenStable();
    (fixture.nativeElement as HTMLElement).querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    backend.expectNone('/api/auth/login');
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('.field-error').length).toBe(2);
  });

  it('stores the user and goes to the safe return url', async () => {
    await submit('/app/account');
    backend.expectOne('/api/auth/login').flush({ success: true, data: aUser() });

    expect(TestBed.inject(SessionStore).user()?.email).toBe('jane@example.com');
    expect(navigateByUrl).toHaveBeenCalledWith('/app/account');
  });

  it('ignores an external return url', async () => {
    await submit('https://evil.com');
    backend.expectOne('/api/auth/login').flush({ success: true, data: aUser() });

    expect(navigateByUrl).toHaveBeenCalledWith('/app');
  });

  it('sends users with a temporary password to the setup page', async () => {
    await submit();
    backend.expectOne('/api/auth/login').flush({ success: true, data: aUser({ firstLogin: true }) });

    expect(navigateByUrl).toHaveBeenCalledWith('/auth/setup-password');
  });

  it('redirects to email verification when the email is not verified', async () => {
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    await submit();
    backend
      .expectOne('/api/auth/login')
      .flush({ success: false, message: 'Non vérifié', data: { userId: 'u-1' } }, { status: 403, statusText: 'Forbidden' });

    expect(navigate).toHaveBeenCalledWith(['/auth/verify-email'], {
      queryParams: { userId: 'u-1', email: 'jane@example.com' },
    });
  });

  it('shows the backend message on invalid credentials', async () => {
    const fixture = await submit();
    backend
      .expectOne('/api/auth/login')
      .flush({ success: false, message: 'Email ou mot de passe incorrect.' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')?.textContent).toContain('Email ou mot de passe incorrect.');
  });
});
