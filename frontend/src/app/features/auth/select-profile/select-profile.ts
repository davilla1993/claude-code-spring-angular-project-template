import { Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthApi } from '../../../core/api/auth-api';
import { SessionStore } from '../../../core/services/session-store';
import { SessionSync } from '../../../core/services/session-sync';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { safeRedirect } from '../../../core/utils/safe-redirect';
import { Role, ROLE_LABELS } from '../../../shared/models/user.model';

/**
 * Choix du profil de travail, après la connexion ou pour en changer :
 * un utilisateur à plusieurs rôles travaille avec un seul profil à la fois.
 */
@Component({
  selector: 'app-select-profile',
  templateUrl: './select-profile.html',
  styleUrl: './select-profile.scss',
})
export class SelectProfile {
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);
  private readonly sessionSync = inject(SessionSync);
  protected readonly session = inject(SessionStore);

  /** Query param ?returnUrl=... */
  readonly returnUrl = input<string>();

  protected readonly roleLabels = ROLE_LABELS;
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected choose(role: Role): void {
    if (role === this.session.user()?.activeRole) {
      this.continue();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    this.authApi.selectProfile(role).subscribe({
      next: (user) => {
        this.session.setUser(user);
        this.sessionSync.announceSessionChange();
        this.continue();
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }

  private continue(): void {
    void this.router.navigateByUrl(safeRedirect(this.returnUrl()));
  }
}
