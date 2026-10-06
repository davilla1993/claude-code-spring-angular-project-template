import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionStore } from '../../../core/services/session-store';
import { APP_NAME } from '../../../shared/constants/app.constants';

/** Page d'accueil publique. Contenu marketing à définir par projet. */
@Component({
  selector: 'app-landing',
  imports: [RouterLink],
  templateUrl: './landing.html',
  styleUrl: './landing.scss',
})
export class Landing {
  protected readonly session = inject(SessionStore);
  protected readonly appName = APP_NAME;
}
