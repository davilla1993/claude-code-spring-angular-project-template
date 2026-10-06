import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionStore } from '../services/session-store';

/**
 * Espace connecté : redirige vers la connexion si aucune session,
 * et vers la définition du mot de passe tant que le mot de passe temporaire n'a pas été remplacé.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const session = inject(SessionStore);
  const router = inject(Router);
  const user = session.user();

  if (!user) {
    return router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } });
  }
  if (user.firstLogin) {
    return router.createUrlTree(['/auth/setup-password']);
  }
  return true;
};

/** Page de définition du mot de passe : uniquement pour un utilisateur connecté avec firstLogin. */
export const firstLoginGuard: CanActivateFn = () => {
  const user = inject(SessionStore).user();
  const router = inject(Router);

  if (!user) {
    return router.createUrlTree(['/auth/login']);
  }
  return user.firstLogin ? true : router.createUrlTree(['/app']);
};

/** Pages réservées aux visiteurs (connexion, inscription...) : un utilisateur connecté est renvoyé vers l'application. */
export const guestGuard: CanActivateFn = () =>
  inject(SessionStore).isAuthenticated() ? inject(Router).createUrlTree(['/app']) : true;
