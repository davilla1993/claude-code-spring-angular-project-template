import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UsersApi } from '../../../core/api/users-api';
import { SessionStore } from '../../../core/services/session-store';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { PASSWORD_RULES, passwordStrength } from '../../../core/validators/password-validators';
import { Role, ROLE_LABELS, ROLES } from '../../../shared/models/user.model';
import { FieldErrorPipe } from '../../../shared/pipes/field-error-pipe';
import { ToastStore } from '../../../shared/toasts/toast-store';

/**
 * Création (route /users/new) ou modification (route /users/:id) d'un utilisateur.
 * En modification, l'email n'est pas modifiable et le mot de passe n'est pas concerné.
 */
@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorPipe],
  templateUrl: './user-form.html',
})
export class UserForm implements OnInit {
  private readonly usersApi = inject(UsersApi);
  private readonly session = inject(SessionStore);
  private readonly router = inject(Router);
  private readonly toasts = inject(ToastStore);

  /** Paramètre de route :id (absent en création). */
  readonly id = input<string>();

  protected readonly isEdit = computed(() => !!this.id());
  protected readonly isSelf = computed(() => this.id() === this.session.user()?.publicId);
  protected readonly roles = ROLES;
  protected readonly roleLabels = ROLE_LABELS;
  protected readonly passwordRules = PASSWORD_RULES;

  protected readonly form = inject(NonNullableFormBuilder).group({
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    temporaryPassword: ['', [Validators.required, passwordStrength()]],
    role: ['USER' as Role, [Validators.required]],
  });
  protected readonly loading = signal(false);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.id();
    if (!id) {
      return;
    }
    this.form.controls.email.disable();
    this.form.controls.temporaryPassword.disable();
    if (this.isSelf()) {
      this.form.controls.role.disable(); // le backend refuse de modifier son propre rôle
    }
    this.loadUser(id);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { firstName, lastName, email, temporaryPassword, role } = this.form.getRawValue();
    const id = this.id();
    const request = id
      ? this.usersApi.update(id, { firstName, lastName, role })
      : this.usersApi.create({ firstName, lastName, email, temporaryPassword, role });

    request.subscribe({
      next: () => {
        this.toasts.success(id ? 'Utilisateur mis à jour.' : 'Utilisateur créé.');
        void this.router.navigateByUrl('/app/admin/users');
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }

  private loadUser(id: string): void {
    this.loading.set(true);
    this.usersApi.get(id).subscribe({
      next: (user) => {
        this.form.patchValue({ firstName: user.firstName, lastName: user.lastName, email: user.email, role: user.role });
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(apiErrorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
