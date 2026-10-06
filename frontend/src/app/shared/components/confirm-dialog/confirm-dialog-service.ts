import { Injectable, signal } from '@angular/core';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  /** Action destructrice : bouton de confirmation en rouge. */
  danger?: boolean;
}

export interface ConfirmRequest extends Required<ConfirmOptions> {
  resolve: (confirmed: boolean) => void;
}

/**
 * Demande de confirmation modale. Affichée par le composant ConfirmDialog (placé une fois dans App).
 *
 * Usage : `if (await this.confirmDialog.confirm({ title: '…', message: '…', danger: true })) { … }`
 */
@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private readonly current = signal<ConfirmRequest | null>(null);

  readonly request = this.current.asReadonly();

  confirm(options: ConfirmOptions): Promise<boolean> {
    // Une seule confirmation à la fois : une demande encore ouverte est considérée comme annulée.
    this.current()?.resolve(false);

    return new Promise<boolean>((resolve) => {
      this.current.set({
        confirmLabel: 'Confirmer',
        cancelLabel: 'Annuler',
        danger: false,
        ...options,
        resolve,
      });
    });
  }

  /** Appelé par le composant lorsque l'utilisateur répond. */
  answer(confirmed: boolean): void {
    const request = this.current();
    if (request) {
      this.current.set(null);
      request.resolve(confirmed);
    }
  }
}
