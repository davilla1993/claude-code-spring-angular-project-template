import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { UsersApi } from '../api/users-api';
import { Permission, User } from '../../shared/models/user.model';

/**
 * Utilisateur courant. Source de vérité de l'état de session côté interface.
 * Ne contient aucun token : la session est portée par les cookies HttpOnly.
 */
@Injectable({ providedIn: 'root' })
export class SessionStore {
  private readonly usersApi = inject(UsersApi);
  private readonly currentUser = signal<User | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  /** Restaure la session au démarrage (cookie encore valide ou refresh possible). Ne lève jamais. */
  async restore(): Promise<void> {
    try {
      this.currentUser.set(await firstValueFrom(this.usersApi.me()));
    } catch {
      this.currentUser.set(null);
    }
  }

  /** Recharge l'utilisateur après une modification de son compte. */
  async reload(): Promise<void> {
    this.currentUser.set(await firstValueFrom(this.usersApi.me()));
  }

  setUser(user: User): void {
    this.currentUser.set(user);
  }

  clear(): void {
    this.currentUser.set(null);
  }

  /** Pour adapter l'interface uniquement : l'autorisation est vérifiée par le backend. */
  hasPermission(permission: Permission): boolean {
    return this.currentUser()?.permissions.includes(permission) ?? false;
  }
}
