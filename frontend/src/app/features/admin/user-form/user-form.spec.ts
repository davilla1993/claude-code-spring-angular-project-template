import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { SessionStore } from '../../../core/services/session-store';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog-service';
import { aUser } from '../../../testing/test-data';
import { UserForm } from './user-form';

describe('UserForm roles', () => {
  let fixture: ComponentFixture<UserForm>;
  let backend: HttpTestingController;
  let confirm: ReturnType<typeof vi.spyOn>;

  const element = () => fixture.nativeElement as HTMLElement;
  const checkboxes = () => [...element().querySelectorAll<HTMLInputElement>('input[type="checkbox"]')];
  /** Envoie le formulaire, puis attend la réponse à la demande de confirmation. */
  const submit = async () => {
    element().querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  };

  function open(id?: string): void {
    fixture = TestBed.createComponent(UserForm);
    if (id) {
      fixture.componentRef.setInput('id', id);
    }
    fixture.detectChanges();
  }

  function fillCreationForm(): void {
    const values: Record<string, string> = {
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      temporaryPassword: 'Temporary1!',
    };
    for (const [name, value] of Object.entries(values)) {
      const input = element().querySelector<HTMLInputElement>(`[formControlName="${name}"]`)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [UserForm],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    confirm = vi.spyOn(TestBed.inject(ConfirmDialogService), 'confirm').mockResolvedValue(true);
    TestBed.inject(SessionStore).setUser(aUser({ publicId: 'me', activeRole: 'ADMIN' }));
  });

  afterEach(() => backend.verify());

  it('creates a user with several roles', async () => {
    open();
    fillCreationForm();

    checkboxes()[0].click(); // ADMIN, en plus de USER coché par défaut
    fixture.detectChanges();
    await submit();

    const request = backend.expectOne('/api/users');
    expect(request.request.body.roles).toEqual(['ADMIN', 'USER']);
    request.flush({ success: true, data: aUser() });
  });

  it('refuses a user without any role', async () => {
    open();
    fillCreationForm();

    checkboxes()[1].click(); // décoche USER
    fixture.detectChanges();
    await submit();
    fixture.detectChanges();

    backend.expectNone('/api/users');
    expect(confirm).not.toHaveBeenCalled();
    expect(element().querySelector('#user-roles-error')).not.toBeNull();
  });

  it('asks for confirmation and sends nothing when it is declined', async () => {
    confirm.mockResolvedValue(false);
    open();
    fillCreationForm();

    await submit();

    expect(confirm).toHaveBeenCalledOnce();
    backend.expectNone('/api/users');
  });

  it('prevents editing own roles but still sends them unchanged', async () => {
    open('me');
    backend.expectOne('/api/users/me').flush({ success: true, data: aUser({ publicId: 'me', roles: ['ADMIN', 'USER'] }) });
    fixture.detectChanges();

    expect(checkboxes().every((checkbox) => checkbox.disabled)).toBe(true);
    expect(checkboxes().every((checkbox) => checkbox.checked)).toBe(true);
    await submit();

    const request = backend.expectOne('/api/users/me');
    expect(request.request.body.roles).toEqual(['ADMIN', 'USER']);
    request.flush({ success: true, data: aUser() });
  });

  it('hides the form when the user cannot be loaded, so their roles cannot be overwritten', () => {
    open('other');
    backend.expectOne('/api/users/other').flush({ success: false, message: 'Erreur' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();

    expect(element().querySelector('form')).toBeNull();
    expect(element().querySelector('[role="alert"]')).not.toBeNull();
    expect(element().querySelector('a[href="/app/admin/users"]')).not.toBeNull();
  });

  it('loads and updates the roles of another user', async () => {
    open('other');
    backend.expectOne('/api/users/other').flush({ success: true, data: aUser({ publicId: 'other', roles: ['USER'] }) });
    fixture.detectChanges();

    checkboxes()[0].click();
    fixture.detectChanges();
    await submit();

    const request = backend.expectOne('/api/users/other');
    expect(request.request.body.roles).toEqual(['ADMIN', 'USER']);
    request.flush({ success: true, data: aUser() });
  });
});
