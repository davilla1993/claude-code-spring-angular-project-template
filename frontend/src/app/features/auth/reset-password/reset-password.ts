import { Component, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth-api';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { matchingFields, PASSWORD_RULES, passwordStrength } from '../../../core/validators/password-validators';
import { ConfirmFieldErrorPipe } from '../../../shared/pipes/confirm-field-error-pipe';
import { FieldErrorPipe } from '../../../shared/pipes/field-error-pipe';
import { ToastStore } from '../../../shared/toasts/toast-store';

@Component({
  selector: 'app-reset-password',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorPipe, ConfirmFieldErrorPipe],
  templateUrl: './reset-password.html',
})
export class ResetPassword implements OnInit {
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);
  private readonly toasts = inject(ToastStore);

  /** Query param transmis par la page "mot de passe oublié". */
  readonly email = input<string>();

  protected readonly passwordRules = PASSWORD_RULES;
  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      newPassword: ['', [Validators.required, passwordStrength()]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: matchingFields('newPassword', 'confirmPassword') },
  );
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const email = this.email();
    if (email) {
      this.form.controls.email.setValue(email);
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { email, code, newPassword } = this.form.getRawValue();
    this.authApi.resetPassword({ email, code, newPassword }).subscribe({
      next: () => {
        this.toasts.success('Mot de passe réinitialisé. Vous pouvez vous connecter.');
        void this.router.navigate(['/auth/login'], { queryParams: { email } });
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }
}
