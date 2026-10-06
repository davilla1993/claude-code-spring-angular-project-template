import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission-guard';
import { APP_NAME } from '../../shared/constants/app.constants';

export const ADMIN_ROUTES: Routes = [
  {
    path: 'users',
    title: `Utilisateurs — ${APP_NAME}`,
    canActivate: [permissionGuard('user:view')],
    loadComponent: () => import('./users-list/users-list').then((m) => m.UsersList),
  },
  {
    path: 'users/new',
    title: `Nouvel utilisateur — ${APP_NAME}`,
    canActivate: [permissionGuard('user:create')],
    loadComponent: () => import('./user-form/user-form').then((m) => m.UserForm),
  },
  {
    path: 'users/:id',
    title: `Modifier l'utilisateur — ${APP_NAME}`,
    canActivate: [permissionGuard('user:update')],
    loadComponent: () => import('./user-form/user-form').then((m) => m.UserForm),
  },
  { path: '', pathMatch: 'full', redirectTo: 'users' },
];
