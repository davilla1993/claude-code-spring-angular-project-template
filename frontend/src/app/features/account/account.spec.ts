import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SessionStore } from '../../core/services/session-store';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog-service';
import { aUser } from '../../testing/test-data';
import { Account } from './account';

describe('Account password change', () => {
  let fixture: ComponentFixture<Account>;
  let backend: HttpTestingController;
  let confirm: ReturnType<typeof vi.spyOn>;

  const element = () => fixture.nativeElement as HTMLElement;

  async function submitNewPassword(): Promise<void> {
    const values: Record<string, string> = {
      currentPassword: 'Current1!',
      newPassword: 'Changed1!',
      confirmPassword: 'Changed1!',
    };
    for (const [name, value] of Object.entries(values)) {
      const input = element().querySelector<HTMLInputElement>(`[formControlName="${name}"]`)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    element().querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Account],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    confirm = vi.spyOn(TestBed.inject(ConfirmDialogService), 'confirm');
    TestBed.inject(SessionStore).setUser(aUser({ activeRole: 'USER' }));
    fixture = TestBed.createComponent(Account);
    fixture.detectChanges();
  });

  afterEach(() => backend.verify());

  it('sends the new password once the change is confirmed', async () => {
    confirm.mockResolvedValue(true);

    await submitNewPassword();

    const request = backend.expectOne((req) => req.url.includes('password'));
    expect(request.request.body).toEqual({ currentPassword: 'Current1!', newPassword: 'Changed1!' });
    request.flush({ success: true, data: null });
  });

  it('sends nothing when the confirmation is declined', async () => {
    confirm.mockResolvedValue(false);

    await submitNewPassword();

    expect(confirm).toHaveBeenCalledOnce();
    backend.expectNone((req) => req.url.includes('password'));
  });
});
