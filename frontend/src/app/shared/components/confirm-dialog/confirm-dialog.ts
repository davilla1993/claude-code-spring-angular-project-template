import { afterRenderEffect, Component, ElementRef, inject, viewChild } from '@angular/core';
import { ConfirmDialogService } from './confirm-dialog-service';

/**
 * Modale de confirmation basée sur l'élément natif <dialog> (showModal) :
 * piège du focus, touche Échap et arrière-plan inerte sont fournis par le navigateur.
 * Le focus initial est placé sur "Annuler" (choix sûr) et restitué à l'élément d'origine à la fermeture.
 */
@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.scss',
})
export class ConfirmDialog {
  protected readonly service = inject(ConfirmDialogService);

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  private readonly cancelButton = viewChild<ElementRef<HTMLButtonElement>>('cancelButton');
  private previouslyFocused: HTMLElement | null = null;

  constructor() {
    // afterRenderEffect : le DOM (dont le bouton Annuler, rendu dans le @if) est à jour.
    afterRenderEffect(() => {
      const request = this.service.request();
      const dialog = this.dialog().nativeElement;

      if (request && !dialog.open) {
        this.previouslyFocused = document.activeElement as HTMLElement | null;
        dialog.showModal();
        this.cancelButton()?.nativeElement.focus();
      } else if (!request && dialog.open) {
        dialog.close();
        this.previouslyFocused?.focus();
        this.previouslyFocused = null;
      }
    });
  }

  protected answer(confirmed: boolean): void {
    this.service.answer(confirmed);
  }

  /** Échap : le navigateur émet "cancel" ; on garde la main sur la fermeture. */
  protected onCancel(event: Event): void {
    event.preventDefault();
    this.answer(false);
  }

  /** Un clic sur le fond (la cible est alors le <dialog> lui-même) vaut annulation. */
  protected onBackdropClick(event: MouseEvent): void {
    if (event.target === this.dialog().nativeElement) {
      this.answer(false);
    }
  }
}
