import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { permissionGuard } from './core/guards/permission-guard';
import { APP_NAME } from './shared/constants/app.constants';

/**
 * - /            : pages publiques (accueil, mentions légales)
 * - /auth/...    : parcours d'authentification
 * - /app/...     : espace connecté (authGuard + permissions)
 */
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: APP_NAME,
    loadComponent: () => import('./features/public/landing/landing').then((m) => m.Landing),
  },
  {
    path: 'legal',
    loadChildren: () => import('./features/public/legal/legal.routes').then((m) => m.LEGAL_ROUTES),
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: 'app',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        title: `Tableau de bord — ${APP_NAME}`,
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'account',
        title: `Mon compte — ${APP_NAME}`,
        loadComponent: () => import('./features/account/account').then((m) => m.Account),
      },
      {
        path: 'admin',
        loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
      },
      {
        path: 'audit',
        title: `Journal d'audit — ${APP_NAME}`,
        canActivate: [permissionGuard('audit:log:view')],
        loadComponent: () => import('./features/auditor/audit-logs/audit-logs').then((m) => m.AuditLogs),
      },
    ],
  },
  {
    path: '**',
    title: `Page introuvable — ${APP_NAME}`,
    loadComponent: () => import('./features/public/not-found/not-found').then((m) => m.NotFound),
  },
];
