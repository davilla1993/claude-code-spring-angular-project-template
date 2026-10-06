import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth-api';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { FieldErrorPipe } from '../../../shared/pipes/field-error-pipe';
import { ToastStore } from '../../../shared/toasts/toast-store';

@Component({
  selector: 'app-forgot-password',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorPipe],
  templateUrl: './forgot-password.html',
})
export class ForgotPassword {
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);
  private readonly toasts = inject(ToastStore);

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
  });
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { email } = this.form.getRawValue();
    this.authApi.forgotPassword(email).subscribe({
      next: () => {
        // Réponse volontairement générique : on ne révèle pas si le compte existe.
        this.toasts.info('Si un compte existe pour cette adresse, un code de réinitialisation a été envoyé.');
        void this.router.navigate(['/auth/reset-password'], { queryParams: { email } });
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }
}
