import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth-api';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { matchingFields, PASSWORD_RULES, passwordStrength } from '../../../core/validators/password-validators';
import { ConfirmFieldErrorPipe } from '../../../shared/pipes/confirm-field-error-pipe';
import { FieldErrorPipe } from '../../../shared/pipes/field-error-pipe';
import { ToastStore } from '../../../shared/toasts/toast-store';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorPipe, ConfirmFieldErrorPipe],
  templateUrl: './register.html',
})
export class Register {
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);
  private readonly toasts = inject(ToastStore);

  protected readonly passwordRules = PASSWORD_RULES;
  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      password: ['', [Validators.required, passwordStrength()]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: matchingFields('password', 'confirmPassword') },
  );
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { firstName, lastName, email, password } = this.form.getRawValue();
    this.authApi.register({ firstName, lastName, email, password }).subscribe({
      next: (user) => {
        this.toasts.success('Compte créé. Un code de vérification vous a été envoyé par email.');
        void this.router.navigate(['/auth/verify-email'], { queryParams: { userId: user.publicId, email: user.email } });
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }
}
