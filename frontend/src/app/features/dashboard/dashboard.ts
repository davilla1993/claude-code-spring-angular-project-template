import { Component, inject } from '@angular/core';
import { SessionStore } from '../../core/services/session-store';

/** Page d'accueil de l'espace connecté. Placeholder à remplacer par le tableau de bord du projet. */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
})
export class Dashboard {
  protected readonly session = inject(SessionStore);
}
