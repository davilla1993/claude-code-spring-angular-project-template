import { Routes } from '@angular/router';
import { firstLoginGuard, guestGuard } from '../../core/guards/auth-guard';
import { APP_NAME } from '../../shared/constants/app.constants';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    title: `Connexion — ${APP_NAME}`,
    canActivate: [guestGuard],
    loadComponent: () => import('./login/login').then((m) => m.Login),
  },
  {
    path: 'register',
    title: `Créer un compte — ${APP_NAME}`,
    canActivate: [guestGuard],
    loadComponent: () => import('./register/register').then((m) => m.Register),
  },
  {
    path: 'verify-email',
    title: `Vérification de l'email — ${APP_NAME}`,
    canActivate: [guestGuard],
    loadComponent: () => import('./verify-email/verify-email').then((m) => m.VerifyEmail),
  },
  {
    path: 'forgot-password',
    title: `Mot de passe oublié — ${APP_NAME}`,
    canActivate: [guestGuard],
    loadComponent: () => import('./forgot-password/forgot-password').then((m) => m.ForgotPassword),
  },
  {
    path: 'reset-password',
    title: `Réinitialiser le mot de passe — ${APP_NAME}`,
    canActivate: [guestGuard],
    loadComponent: () => import('./reset-password/reset-password').then((m) => m.ResetPassword),
  },
  {
    path: 'setup-password',
    title: `Définir votre mot de passe — ${APP_NAME}`,
    canActivate: [firstLoginGuard],
    loadComponent: () => import('./setup-password/setup-password').then((m) => m.SetupPassword),
  },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
];
