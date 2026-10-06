import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormGroupDirective, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsersApi } from '../../core/api/users-api';
import { SessionStore } from '../../core/services/session-store';
import { apiErrorMessage } from '../../core/utils/api-error';
import { matchingFields, PASSWORD_RULES, passwordStrength } from '../../core/validators/password-validators';
import { ROLE_LABELS } from '../../shared/models/user.model';
import { ConfirmFieldErrorPipe } from '../../shared/pipes/confirm-field-error-pipe';
import { FieldErrorPipe } from '../../shared/pipes/field-error-pipe';
import { ToastStore } from '../../shared/toasts/toast-store';

/** Profil de l'utilisateur connecté et changement de mot de passe. */
@Component({
  selector: 'app-account',
  imports: [ReactiveFormsModule, DatePipe, FieldErrorPipe, ConfirmFieldErrorPipe],
  templateUrl: './account.html',
  styleUrl: './account.scss',
})
export class Account {
  private readonly usersApi = inject(UsersApi);
  private readonly toasts = inject(ToastStore);
  protected readonly session = inject(SessionStore);

  protected readonly roleLabels = ROLE_LABELS;
  protected readonly passwordRules = PASSWORD_RULES;
  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      currentPassword: ['', [Validators.required, Validators.maxLength(72)]],
      newPassword: ['', [Validators.required, passwordStrength()]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: matchingFields('newPassword', 'confirmPassword') },
  );
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(formDirective: FormGroupDirective): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { currentPassword, newPassword } = this.form.getRawValue();
    this.usersApi.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.submitting.set(false);
        formDirective.resetForm();
        this.toasts.success('Mot de passe mis à jour.');
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }
}
