import { Component, DestroyRef, ElementRef, inject, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, skip } from 'rxjs';
import { Footer } from './core/layout/footer/footer';
import { Header } from './core/layout/header/header';
import { SessionSync } from './core/services/session-sync';
import { ConfirmDialog } from './shared/components/confirm-dialog/confirm-dialog';
import { ToastContainer } from './shared/toasts/toast-container/toast-container';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Footer, ToastContainer, ConfirmDialog],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly main = viewChild.required<ElementRef<HTMLElement>>('main');

  constructor() {
    inject(SessionSync); // écoute les changements de session faits dans un autre onglet

    // Accessibilité : après chaque navigation (hors chargement initial), le focus passe au contenu principal
    // pour que les lecteurs d'écran annoncent la nouvelle page.
    inject(Router)
      .events.pipe(
        filter((event) => event instanceof NavigationEnd),
        skip(1),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe(() => this.main().nativeElement.focus());
  }
}
