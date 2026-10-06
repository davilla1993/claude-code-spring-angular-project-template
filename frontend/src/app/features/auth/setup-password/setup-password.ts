import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UsersApi } from '../../../core/api/users-api';
import { SessionStore } from '../../../core/services/session-store';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { matchingFields, PASSWORD_RULES, passwordStrength } from '../../../core/validators/password-validators';
import { ConfirmFieldErrorPipe } from '../../../shared/pipes/confirm-field-error-pipe';
import { FieldErrorPipe } from '../../../shared/pipes/field-error-pipe';
import { ToastStore } from '../../../shared/toasts/toast-store';

/** Première connexion : remplacement du mot de passe temporaire fourni par un administrateur. */
@Component({
  selector: 'app-setup-password',
  imports: [ReactiveFormsModule, FieldErrorPipe, ConfirmFieldErrorPipe],
  templateUrl: './setup-password.html',
})
export class SetupPassword {
  private readonly usersApi = inject(UsersApi);
  private readonly session = inject(SessionStore);
  private readonly router = inject(Router);
  private readonly toasts = inject(ToastStore);

  protected readonly passwordRules = PASSWORD_RULES;
  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      newPassword: ['', [Validators.required, passwordStrength()]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: matchingFields('newPassword', 'confirmPassword') },
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

    this.usersApi.setupPassword(this.form.getRawValue().newPassword).subscribe({
      next: () => void this.onPasswordSet(),
      error: (err: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }

  private async onPasswordSet(): Promise<void> {
    this.toasts.success('Mot de passe enregistré.');
    try {
      await this.session.reload(); // met à jour firstLogin, sinon authGuard renverrait ici
    } catch {
      this.session.clear(); // session expirée entre-temps : authGuard redirigera vers la connexion
    }
    void this.router.navigateByUrl('/app');
  }
}
