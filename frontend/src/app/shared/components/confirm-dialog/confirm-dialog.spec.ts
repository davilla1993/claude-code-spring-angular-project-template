import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfirmDialog } from './confirm-dialog';
import { ConfirmDialogService } from './confirm-dialog-service';

/** Hôte de test : un bouton déclencheur pour vérifier la restitution du focus. */
@Component({
  imports: [ConfirmDialog],
  template: `<button id="trigger" type="button">Supprimer</button><app-confirm-dialog />`,
})
class Host {}

describe('ConfirmDialog', () => {
  let fixture: ComponentFixture<Host>;
  let service: ConfirmDialogService;
  let element: HTMLElement;

  beforeAll(() => {
    // jsdom n'implémente pas toujours <dialog> : simulation minimale de showModal/close.
    const proto = HTMLDialogElement.prototype;
    if (!proto.showModal) {
      proto.showModal = function (this: HTMLDialogElement) {
        this.setAttribute('open', '');
      };
      proto.close = function (this: HTMLDialogElement) {
        this.removeAttribute('open');
      };
    }
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Host] }).compileComponents();
    fixture = TestBed.createComponent(Host);
    service = TestBed.inject(ConfirmDialogService);
    element = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  const dialog = () => element.querySelector('dialog')!;
  const buttons = () => Array.from(dialog().querySelectorAll('button'));

  /** Retourne la promesse dans un objet : `await` ne doit pas attendre la réponse de l'utilisateur. */
  async function open(danger = false): Promise<{ answer: Promise<boolean> }> {
    element.querySelector<HTMLButtonElement>('#trigger')!.focus();
    const answer = service.confirm({ title: 'Supprimer ?', message: 'Action définitive.', confirmLabel: 'Supprimer', danger });
    await fixture.whenStable();
    return { answer };
  }

  it('opens a labelled modal with the requested content, focus on the safe choice', async () => {
    await open(true);

    expect(dialog().open).toBe(true);
    expect(dialog().getAttribute('role')).toBe('alertdialog');
    expect(element.querySelector('#confirm-dialog-title')?.textContent).toContain('Supprimer ?');
    expect(element.querySelector('#confirm-dialog-message')?.textContent).toContain('Action définitive.');
    expect(buttons().map((b) => b.textContent?.trim())).toEqual(['Annuler', 'Supprimer']);
    expect(buttons()[1].classList).toContain('btn--danger');
    expect(document.activeElement).toBe(buttons()[0]);
  });

  it('resolves true on confirm, closes and restores focus', async () => {
    const { answer } = await open();
    buttons()[1].click();
    await fixture.whenStable();

    expect(await answer).toBe(true);
    expect(dialog().open).toBe(false);
    expect(document.activeElement?.id).toBe('trigger');
  });

  it('resolves false on cancel button', async () => {
    const { answer } = await open();
    buttons()[0].click();

    expect(await answer).toBe(false);
  });

  it('resolves false on Escape (cancel event)', async () => {
    const { answer } = await open();
    dialog().dispatchEvent(new Event('cancel', { cancelable: true }));

    expect(await answer).toBe(false);
  });

  it('resolves false on backdrop click but not on content click', async () => {
    const { answer } = await open();
    let settled = false;
    void answer.then(() => (settled = true));

    element.querySelector<HTMLElement>('#confirm-dialog-message')!.click();
    await fixture.whenStable();
    expect(settled).toBe(false);

    dialog().click();
    expect(await answer).toBe(false);
  });

  it('cancels a pending request when a new one is opened', async () => {
    const { answer: first } = await open();
    const second = service.confirm({ title: 'Autre', message: '…' });

    expect(await first).toBe(false);
    service.answer(true);
    expect(await second).toBe(true);
  });
});
