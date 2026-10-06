import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth-api';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { FieldErrorPipe } from '../../../shared/pipes/field-error-pipe';
import { ToastStore } from '../../../shared/toasts/toast-store';

@Component({
  selector: 'app-verify-email',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorPipe],
  templateUrl: './verify-email.html',
})
export class VerifyEmail {
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);
  private readonly toasts = inject(ToastStore);

  /** Query params transmis par l'inscription ou la connexion. */
  readonly userId = input<string>();
  readonly email = input<string>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
  });
  protected readonly submitting = signal(false);
  protected readonly resending = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(): void {
    const userId = this.userId();
    if (this.form.invalid || !userId) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    this.authApi.verifyEmail(userId, this.form.getRawValue().code).subscribe({
      next: () => {
        this.toasts.success('Adresse email vérifiée. Vous pouvez vous connecter.');
        void this.router.navigate(['/auth/login'], { queryParams: { email: this.email() } });
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }

  protected resend(): void {
    const email = this.email();
    if (!email) {
      return;
    }
    this.resending.set(true);
    this.authApi.resendVerification(email).subscribe({
      next: () => {
        this.resending.set(false);
        this.toasts.info('Si un compte non vérifié existe, un nouveau code a été envoyé.');
      },
      error: (err: unknown) => {
        this.resending.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }
}
