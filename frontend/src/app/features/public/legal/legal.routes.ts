import { Routes } from '@angular/router';
import { APP_NAME } from '../../../shared/constants/app.constants';

/** Pages légales : contenu placeholder à rédiger pour chaque projet (validation juridique requise). */
export const LEGAL_ROUTES: Routes = [
  {
    path: 'mentions-legales',
    title: `Mentions légales — ${APP_NAME}`,
    loadComponent: () => import('./mentions-legales/mentions-legales').then((m) => m.MentionsLegales),
  },
  {
    path: 'conditions-utilisation',
    title: `Conditions d'utilisation — ${APP_NAME}`,
    loadComponent: () =>
      import('./conditions-utilisation/conditions-utilisation').then((m) => m.ConditionsUtilisation),
  },
  {
    path: 'politique-confidentialite',
    title: `Politique de confidentialité — ${APP_NAME}`,
    loadComponent: () =>
      import('./politique-confidentialite/politique-confidentialite').then((m) => m.PolitiqueConfidentialite),
  },
];
