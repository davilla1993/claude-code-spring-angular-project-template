import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth-api';
import { SessionStore } from '../../../core/services/session-store';
import { apiErrorData, apiErrorMessage } from '../../../core/utils/api-error';
import { safeRedirect } from '../../../core/utils/safe-redirect';
import { User } from '../../../shared/models/user.model';
import { FieldErrorPipe } from '../../../shared/pipes/field-error-pipe';
import { ToastStore } from '../../../shared/toasts/toast-store';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorPipe],
  templateUrl: './login.html',
})
export class Login implements OnInit {
  private readonly authApi = inject(AuthApi);
  private readonly session = inject(SessionStore);
  private readonly router = inject(Router);
  private readonly toasts = inject(ToastStore);

  /** Query params (?returnUrl=...&email=...). */
  readonly returnUrl = input<string>();
  readonly email = input<string>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required, Validators.maxLength(72)]],
  });
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

    const { email, password } = this.form.getRawValue();
    this.authApi.login(email, password).subscribe({
      next: (user) => this.onLoggedIn(user),
      error: (err: unknown) => this.onError(err, email),
    });
  }

  private onLoggedIn(user: User): void {
    this.session.setUser(user);
    void this.router.navigateByUrl(user.firstLogin ? '/auth/setup-password' : safeRedirect(this.returnUrl()));
  }

  private onError(err: unknown, email: string): void {
    this.submitting.set(false);
    const unverified = apiErrorData<{ userId?: string }>(err)?.userId;
    if (err instanceof HttpErrorResponse && err.status === HttpStatusCode.Forbidden && unverified) {
      this.toasts.info('Un code de vérification vient de vous être envoyé par email.');
      void this.router.navigate(['/auth/verify-email'], { queryParams: { userId: unverified, email } });
      return;
    }
    this.error.set(apiErrorMessage(err));
  }
}
