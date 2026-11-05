import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthApi } from '../../api/auth-api';
import { SessionStore } from '../../services/session-store';
import { SessionSync } from '../../services/session-sync';
import { APP_NAME } from '../../../shared/constants/app.constants';
import { ROLE_LABELS } from '../../../shared/models/user.model';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  protected readonly session = inject(SessionStore);
  private readonly sessionSync = inject(SessionSync);
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);

  protected readonly appName = APP_NAME;
  protected readonly roleLabels = ROLE_LABELS;

  protected logout(): void {
    // La session locale est vidée même si l'appel échoue : les cookies expireront d'eux-mêmes.
    this.authApi.logout().subscribe({
      next: () => this.endSession(),
      error: () => this.endSession(),
    });
  }

  private endSession(): void {
    this.session.clear();
    this.sessionSync.announceSessionChange();
    void this.router.navigateByUrl('/');
  }
}
