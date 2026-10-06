import { inject, Injectable } from '@angular/core';
import { finalize, Observable, shareReplay, tap } from 'rxjs';
import { AuthApi } from '../api/auth-api';
import { User } from '../../shared/models/user.model';
import { SessionStore } from './session-store';

/**
 * Rafraîchit la session en garantissant un seul appel /auth/refresh à la fois.
 * Indispensable : le backend révoque toutes les sessions si un refresh token déjà
 * utilisé est présenté (cas de deux refresh concurrents).
 */
@Injectable({ providedIn: 'root' })
export class TokenRefresher {
  private readonly authApi = inject(AuthApi);
  private readonly session = inject(SessionStore);
  private inFlight: Observable<User> | null = null;

  refresh(): Observable<User> {
    if (!this.inFlight) {
      this.inFlight = this.authApi.refresh().pipe(
        tap((user) => this.session.setUser(user)),
        finalize(() => (this.inFlight = null)),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }
    return this.inFlight;
  }
}
