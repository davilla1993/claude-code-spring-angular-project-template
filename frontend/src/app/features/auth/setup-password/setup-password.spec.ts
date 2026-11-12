import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog-service';
import { SetupPassword } from './setup-password';

describe('SetupPassword', () => {
  let fixture: ComponentFixture<SetupPassword>;
  let backend: HttpTestingController;
  let confirm: ReturnType<typeof vi.spyOn>;

  const element = () => fixture.nativeElement as HTMLElement;

  async function submitPassword(): Promise<void> {
    for (const name of ['newPassword', 'confirmPassword']) {
      const input = element().querySelector<HTMLInputElement>(`[formControlName="${name}"]`)!;
      input.value = 'Changed1!';
      input.dispatchEvent(new Event('input'));
    }
    element().querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SetupPassword],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    confirm = vi.spyOn(TestBed.inject(ConfirmDialogService), 'confirm');
    fixture = TestBed.createComponent(SetupPassword);
    fixture.detectChanges();
  });

  afterEach(() => backend.verify());

  it('sends nothing when the confirmation is declined', async () => {
    confirm.mockResolvedValue(false);

    await submitPassword();

    expect(confirm).toHaveBeenCalledOnce();
    backend.expectNone('/api/users/me/setup-password');
  });

  it('sends the password once confirmed and shows the server error', async () => {
    confirm.mockResolvedValue(true);

    await submitPassword();
    backend
      .expectOne('/api/users/me/setup-password')
      .flush({ success: false, message: 'Refusé' }, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();

    expect(element().querySelector('[role="alert"]')).not.toBeNull();
  });
});
