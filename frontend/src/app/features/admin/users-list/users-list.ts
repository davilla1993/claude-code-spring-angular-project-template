import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UsersApi } from '../../../core/api/users-api';
import { SessionStore } from '../../../core/services/session-store';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog-service';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { DEFAULT_PAGE_SIZE } from '../../../shared/constants/app.constants';
import { PageResponse } from '../../../shared/models/api-response.model';
import { Role, ROLE_LABELS, ROLES, User } from '../../../shared/models/user.model';
import { ToastStore } from '../../../shared/toasts/toast-store';

@Component({
  selector: 'app-users-list',
  imports: [FormsModule, RouterLink, Pagination],
  templateUrl: './users-list.html',
  styleUrl: './users-list.scss',
})
export class UsersList implements OnInit {
  private readonly usersApi = inject(UsersApi);
  private readonly toasts = inject(ToastStore);
  private readonly confirmDialog = inject(ConfirmDialogService);
  protected readonly session = inject(SessionStore);

  protected readonly roles = ROLES;
  protected readonly roleLabels = ROLE_LABELS;

  protected readonly roleFilter = signal<Role | ''>('');
  protected readonly page = signal<PageResponse<User> | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Mot de passe temporaire généré, affiché une seule fois. */
  protected readonly temporaryPassword = signal<{ user: User; password: string } | null>(null);

  ngOnInit(): void {
    this.load(0);
  }

  protected load(pageIndex: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.usersApi
      .list({ page: pageIndex, size: DEFAULT_PAGE_SIZE, sort: 'createdAt,desc' }, this.roleFilter() || undefined)
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: (err: unknown) => {
          this.error.set(apiErrorMessage(err));
          this.loading.set(false);
        },
      });
  }

  protected onRoleFilterChange(role: Role | ''): void {
    this.roleFilter.set(role);
    this.load(0);
  }

  protected isSelf(user: User): boolean {
    return this.session.user()?.publicId === user.publicId;
  }

  protected async toggleActive(user: User): Promise<void> {
    const activate = !user.active;
    if (
      !activate &&
      !(await this.confirmDialog.confirm({
        title: 'Désactiver le compte',
        message: `${user.firstName} ${user.lastName} ne pourra plus se connecter et ses sessions en cours seront fermées.`,
        confirmLabel: 'Désactiver',
        danger: true,
      }))
    ) {
      return;
    }
    this.usersApi.setActive(user.publicId, activate).subscribe({
      next: () => {
        this.toasts.success(activate ? 'Utilisateur activé.' : 'Utilisateur désactivé.');
        this.reloadCurrentPage();
      },
      error: (err: unknown) => this.toasts.error(apiErrorMessage(err)),
    });
  }

  protected async resetPassword(user: User): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Réinitialiser le mot de passe',
      message: `Un mot de passe temporaire va être généré pour ${user.firstName} ${user.lastName}. Ses sessions en cours seront fermées.`,
      confirmLabel: 'Réinitialiser',
      danger: true,
    });
    if (!confirmed) {
      return;
    }
    this.usersApi.resetPassword(user.publicId).subscribe({
      next: (password) => {
        this.temporaryPassword.set({ user, password });
        this.reloadCurrentPage();
      },
      error: (err: unknown) => this.toasts.error(apiErrorMessage(err)),
    });
  }

  protected copyTemporaryPassword(password: string): void {
    navigator.clipboard.writeText(password).then(
      () => this.toasts.success('Mot de passe copié.'),
      () => this.toasts.error('Copie impossible : sélectionnez le mot de passe manuellement.'),
    );
  }

  private reloadCurrentPage(): void {
    this.load(this.page()?.page ?? 0);
  }
}
